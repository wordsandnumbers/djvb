package com.vpo.djvoxbox.faye;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.http.jetty.JettyHttpClientTransport;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.websocket.jetty.JettyWebSocketTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Lifecycle owner of the single {@link BayeuxClient} used to subscribe to the
 * upstream Lyrical Systems Faye server.
 *
 * <p>Why one client for many rooms: Bayeux is explicitly designed for
 * many-channels-over-one-connection. Each {@code UserQueue} room subscribes
 * to its own channel {@code /karaoke/{orgId}/rooms/{roomCode}/queue} on the
 * shared connection. Subscribing or unsubscribing a channel is a
 * {@code /meta/subscribe} or {@code /meta/unsubscribe} message on the
 * connection — no new TCP socket.
 *
 * <p>Lifecycle:
 * <ul>
 *   <li>{@link #start()} (PostConstruct) — build the client with WS as
 *       primary transport and HTTP long-poll as fallback, register a
 *       handshake listener that (re)seeds the subscription set, then call
 *       {@code handshake()}.</li>
 *   <li>{@link #stop()} (PreDestroy) — unsubscribe everything and disconnect
 *       cleanly. Jetty {@code HttpClient} and {@code WebSocketClient} are
 *       stopped by Spring via their own bean definitions in
 *       {@link FayeConfig}.</li>
 * </ul>
 *
 * <p>Reconnection: CometD's {@code /meta/connect} loop handles transient
 * drops automatically. For drops long enough that the server forgets the
 * client (default ~45s timeout), the {@code /meta/handshake} listener fires
 * again on successful re-handshake and we re-seed subscriptions from
 * {@link UserQueueRepository} (instead of relying on CometD remembering the
 * channel listeners across re-handshakes).
 *
 * <p>Thread-safety: {@link #subscribedRooms} is guarded by a single
 * {@link ReentrantLock}. The {@code BayeuxClient} itself is internally
 * thread-safe; subscribe/unsubscribe calls are cheap and lock-free at the
 * client level.
 */
@Component
public class FayeQueueSubscriber {

    private static final Logger log = LoggerFactory.getLogger(FayeQueueSubscriber.class);

    private final HttpClient httpClient;
    private final WebSocketClient webSocketClient;
    private final UserQueueRepository userQueueRepository;
    private final QueueEventHandler queueEventHandler;

    @Value("${vb.fayeUrl:wss://app.lyricalsystems.com:30008/faye}")
    private String fayeUrl;

    @Value("${vb.organization}")
    private String vbOrganization;

    @Value("${faye.handshakeTimeoutMs:15000}")
    private long handshakeTimeoutMs;

    private final Set<String> subscribedRooms = new HashSet<>();
    private final ReentrantLock subscriptionLock = new ReentrantLock();

    // Listener used for every channel — looking it up from the Faye channel
    // path so we can route to QueueEventHandler.notify.
    private final ClientSessionChannel.MessageListener queueChannelListener = this::onChannelMessage;

    private volatile BayeuxClient bayeuxClient;

    @Autowired
    public FayeQueueSubscriber(
            HttpClient fayeHttpClient,
            WebSocketClient fayeWebSocketClient,
            UserQueueRepository userQueueRepository,
            QueueEventHandler queueEventHandler) {
        this.httpClient = fayeHttpClient;
        this.webSocketClient = fayeWebSocketClient;
        this.userQueueRepository = userQueueRepository;
        this.queueEventHandler = queueEventHandler;
    }

    /**
     * Build the {@link BayeuxClient}, attach lifecycle listeners, kick off
     * the handshake. Tolerant of upstream unreachability — the handshake
     * runs asynchronously, so a slow/dead Faye does not block context load.
     */
    @PostConstruct
    public void start() {
        try {
            // Per Bayeux 1.0 spec: a BayeuxClient takes a primary transport
            // and zero or more fallbacks. WS-first, HTTP-fallback is the
            // canonical setup.
            ClientTransport wsTransport = new JettyWebSocketTransport(null, null, webSocketClient);
            ClientTransport httpTransport = new JettyHttpClientTransport(null, httpClient);
            BayeuxClient client = new BayeuxClient(fayeUrl, wsTransport, httpTransport);

            // META_HANDSHAKE is /meta/handshake — fired once per successful
            // handshake (initial connect AND after re-handshake following a
            // server-forgotten reconnect). Re-seeding here makes subscription
            // recovery resilient to long drops.
            client.getChannel(Channel.META_HANDSHAKE).addListener(
                    (ClientSessionChannel.MessageListener) this::onHandshake);

            // META_CONNECT is /meta/connect — fired on every long-poll-style
            // heartbeat. Logging at trace so we can tell from logs whether
            // the loop is alive without flooding.
            client.getChannel(Channel.META_CONNECT).addListener(
                    (ClientSessionChannel.MessageListener) (ch, m) -> {
                        if (!m.isSuccessful()) {
                            log.info("Faye /meta/connect failed: {}", m);
                        }
                    });

            // META_DISCONNECT for completeness — observed only on graceful shutdown.
            client.getChannel(Channel.META_DISCONNECT).addListener(
                    (ClientSessionChannel.MessageListener) (ch, m) -> log.info("Faye /meta/disconnect: {}", m.isSuccessful() ? "ok" : m));

            this.bayeuxClient = client;
            client.handshake();
            log.info("FayeQueueSubscriber: handshake initiated url={}", fayeUrl);
        } catch (Exception e) {
            // Don't fail context load on an unreachable Faye — the system
            // can still serve HTTP traffic and the reconciliation poll
            // continues to keep state fresh.
            log.warn("FayeQueueSubscriber: start failed, continuing without WS", e);
        }
    }

    @PreDestroy
    public void stop() {
        BayeuxClient client = this.bayeuxClient;
        if (client == null) {
            return;
        }
        try {
            subscriptionLock.lock();
            try {
                for (String room : subscribedRooms) {
                    try {
                        client.getChannel(channelFor(room)).unsubscribe(queueChannelListener);
                    } catch (Exception e) {
                        log.debug("unsubscribe error room={}", room, e);
                    }
                }
                subscribedRooms.clear();
            } finally {
                subscriptionLock.unlock();
            }
            client.disconnect(2000);
        } catch (Exception e) {
            log.warn("FayeQueueSubscriber: stop encountered error", e);
        }
    }

    /**
     * Subscribe to {@code roomCode}'s channel if not already subscribed.
     * Called both during {@link #start()} (seeding from existing
     * UserQueues), from
     * {@link com.vpo.djvoxbox.web.QueueController#createUserQueue} when a
     * user joins a new room, and from the META_HANDSHAKE listener after a
     * reconnect.
     */
    public void ensureSubscribed(String roomCode) {
        if (roomCode == null || roomCode.isEmpty()) {
            return;
        }
        BayeuxClient client = this.bayeuxClient;
        if (client == null) {
            return;
        }
        subscriptionLock.lock();
        try {
            if (subscribedRooms.contains(roomCode)) {
                return;
            }
            String channel = channelFor(roomCode);
            client.getChannel(channel).subscribe(queueChannelListener);
            subscribedRooms.add(roomCode);
            log.info("subscribed to {}", channel);
        } finally {
            subscriptionLock.unlock();
        }
    }

    /**
     * Unsubscribe iff no other UserQueue still references {@code roomCode}.
     * The caller decides "no other reference"; this method simply forgets
     * the subscription unconditionally. Safe to call on a never-subscribed
     * room.
     */
    public void maybeUnsubscribe(String roomCode) {
        if (roomCode == null || roomCode.isEmpty()) {
            return;
        }
        BayeuxClient client = this.bayeuxClient;
        if (client == null) {
            return;
        }
        subscriptionLock.lock();
        try {
            if (!subscribedRooms.remove(roomCode)) {
                return;
            }
            String channel = channelFor(roomCode);
            client.getChannel(channel).unsubscribe(queueChannelListener);
            log.info("unsubscribed from {}", channel);
        } finally {
            subscriptionLock.unlock();
        }
    }

    /**
     * Listener for every queue channel. Routes the raw payload to
     * {@link QueueEventHandler#notify}, which is responsible for parsing the
     * event discriminator and executing the two-phase logic.
     */
    private void onChannelMessage(ClientSessionChannel channel, Message message) {
        String roomCode = extractRoomCode(channel.getId());
        if (roomCode == null) {
            return;
        }
        queueEventHandler.notify(roomCode, message.getData());
    }

    /**
     * /meta/handshake listener: reseeds subscriptions from the current
     * UserQueue set whenever a fresh handshake succeeds. This covers both
     * the initial connection and recovery after the server forgets the
     * client across a long drop.
     */
    private void onHandshake(ClientSessionChannel channel, Message message) {
        if (!message.isSuccessful()) {
            log.warn("Faye handshake failed: {}", message);
            return;
        }
        log.info("Faye handshake successful clientId={}", message.getClientId());
        reseedSubscriptions();
    }

    /**
     * Pull every distinct {@code roomCode} from UserQueueRepository whose
     * organization matches ours, and ensure each one is subscribed. Used at
     * startup and after every successful handshake.
     */
    void reseedSubscriptions() {
        try {
            List<UserQueue> all = userQueueRepository.findAll();
            Set<String> wanted = new HashSet<>();
            for (UserQueue uq : all) {
                if (uq.getRoomCode() == null) continue;
                if (uq.getOrganization() != null && vbOrganization != null
                        && !uq.getOrganization().equals(vbOrganization)) {
                    continue;
                }
                wanted.add(uq.getRoomCode());
            }
            // After a handshake the BayeuxClient has a fresh client session
            // so prior subscriptions are gone server-side; clear our local
            // set and re-subscribe.
            subscriptionLock.lock();
            try {
                subscribedRooms.clear();
            } finally {
                subscriptionLock.unlock();
            }
            for (String room : wanted) {
                ensureSubscribed(room);
            }
        } catch (Exception e) {
            log.warn("reseedSubscriptions failed", e);
        }
    }

    private String channelFor(String roomCode) {
        return "/karaoke/" + vbOrganization + "/rooms/" + roomCode + "/queue";
    }

    /**
     * Parse {@code /karaoke/{org}/rooms/{room}/queue} → {@code room}. Returns
     * {@code null} for malformed channels.
     */
    static String extractRoomCode(String channelId) {
        if (channelId == null) return null;
        String marker = "/rooms/";
        int start = channelId.indexOf(marker);
        if (start < 0) return null;
        int from = start + marker.length();
        int end = channelId.indexOf('/', from);
        if (end < 0) return channelId.substring(from);
        return channelId.substring(from, end);
    }

    // Visible-for-test
    Set<String> getSubscribedRoomsSnapshot() {
        subscriptionLock.lock();
        try {
            return new HashSet<>(subscribedRooms);
        } finally {
            subscriptionLock.unlock();
        }
    }

    // Visible-for-test
    void setBayeuxClientForTest(BayeuxClient client) {
        this.bayeuxClient = client;
    }
}
