package com.vpo.djvoxbox.faye;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring beans for the Faye/Bayeux client stack.
 *
 * <p>Three layers are exposed as Spring-managed singletons so they can be
 * swapped in tests and so their lifecycles (start / stop) are tied to the
 * application context:
 * <ol>
 *   <li>{@link HttpClient} — Jetty's low-level HTTP client. Used by both the
 *       HTTP long-poll fallback transport and as the host for the WebSocket
 *       upgrade handshake.</li>
 *   <li>{@link WebSocketClient} — Jetty's WebSocket client, layered on top of
 *       the {@code HttpClient}. CometD's {@code JettyWebSocketTransport}
 *       consumes this.</li>
 * </ol>
 *
 * <p>The actual {@code BayeuxClient} is constructed lazily inside
 * {@link FayeQueueSubscriber#start()} so it can be re-handshaked on
 * reconnect without disturbing the bean graph.
 *
 * <p>Both beans declare {@code initMethod} / {@code destroyMethod} so Spring
 * manages start/stop — there is no separate {@code @PostConstruct} needed.
 */
@Configuration
public class FayeConfig {

    @Value("${faye.handshakeTimeoutMs:15000}")
    private long handshakeTimeoutMs;

    /**
     * Jetty {@link HttpClient}, TLS-enabled and started on bean init. Used as
     * both the HTTP transport carrier and the underlying transport for the
     * WebSocket upgrade.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public HttpClient fayeHttpClient() {
        // Default HttpClient trusts the JVM's truststore; the Lyrical Systems
        // Faye endpoint uses a public CA so no additional trust material is
        // required.
        HttpClient httpClient = new HttpClient();
        httpClient.setConnectTimeout(handshakeTimeoutMs);
        httpClient.setIdleTimeout(0); // server-side advice drives keepalive
        return httpClient;
    }

    /**
     * Jetty {@link WebSocketClient}. CometD's
     * {@code JettyWebSocketTransport} adopts this client; we do not subscribe
     * to WS frames directly here — that's CometD's job.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public WebSocketClient fayeWebSocketClient(HttpClient fayeHttpClient) {
        return new WebSocketClient(fayeHttpClient);
    }
}
