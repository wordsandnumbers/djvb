package com.vpo.djvoxbox.faye;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;

/**
 * Channel-management behaviors of {@link FayeQueueSubscriber}.
 *
 * <p>The BayeuxClient itself is mocked — we don't run a real Faye handshake.
 * What we care about is that subscribe/unsubscribe are issued for the right
 * channels in the right idempotent way.
 */
@ExtendWith(MockitoExtension.class)
class FayeQueueSubscriberTest {

    private static final String ORG = "d00d11e681934f4688fdce9cebd5afce";

    @Mock UserQueueRepository userQueueRepository;
    @Mock QueueEventHandler queueEventHandler;
    @Mock BayeuxClient bayeuxClient;

    private FayeQueueSubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new FayeQueueSubscriber(null, null, userQueueRepository, queueEventHandler);
        ReflectionTestUtils.setField(subscriber, "vbOrganization", ORG);
        ReflectionTestUtils.setField(subscriber, "fayeUrl", "wss://example.invalid/faye");
        subscriber.setBayeuxClientForTest(bayeuxClient);
    }

    @Test
    void ensureSubscribed_isIdempotent() {
        ClientSessionChannel channel = mockChannel("MCHQ");

        subscriber.ensureSubscribed("MCHQ");
        subscriber.ensureSubscribed("MCHQ");

        verify(channel, times(1)).subscribe(any(ClientSessionChannel.MessageListener.class));
        assertThat(subscriber.getSubscribedRoomsSnapshot()).containsExactly("MCHQ");
    }

    @Test
    void ensureSubscribed_distinctRooms_subscribeEach() {
        ClientSessionChannel ch1 = mockChannel("MCHQ");
        ClientSessionChannel ch2 = mockChannel("WWWF");

        subscriber.ensureSubscribed("MCHQ");
        subscriber.ensureSubscribed("WWWF");

        verify(ch1, times(1)).subscribe(any(ClientSessionChannel.MessageListener.class));
        verify(ch2, times(1)).subscribe(any(ClientSessionChannel.MessageListener.class));
        assertThat(subscriber.getSubscribedRoomsSnapshot()).containsExactlyInAnyOrder("MCHQ", "WWWF");
    }

    @Test
    void ensureSubscribed_emptyOrNull_noOp() {
        subscriber.ensureSubscribed(null);
        subscriber.ensureSubscribed("");
        assertThat(subscriber.getSubscribedRoomsSnapshot()).isEmpty();
    }

    @Test
    void maybeUnsubscribe_clearsAndCallsUnsubscribe() {
        ClientSessionChannel channel = mockChannel("MCHQ");
        subscriber.ensureSubscribed("MCHQ");

        subscriber.maybeUnsubscribe("MCHQ");

        verify(channel).unsubscribe(any(ClientSessionChannel.MessageListener.class));
        assertThat(subscriber.getSubscribedRoomsSnapshot()).isEmpty();
    }

    @Test
    void maybeUnsubscribe_neverSubscribed_isNoOp() {
        // Nothing to assert against the BayeuxClient (its getChannel is
        // overloaded so the verify call is ambiguous); the test verifies
        // the method does not throw and leaves the subscribed-set empty.
        subscriber.maybeUnsubscribe("GHOST");
        assertThat(subscriber.getSubscribedRoomsSnapshot()).isEmpty();
    }

    @Test
    void reseedSubscriptions_subscribesEveryDistinctRoomFromRepo() {
        ClientSessionChannel ch1 = mockChannel("MCHQ");
        ClientSessionChannel ch2 = mockChannel("WWWF");
        when(userQueueRepository.findAll()).thenReturn(List.of(
                uq("MCHQ", ORG),
                uq("WWWF", ORG),
                uq("MCHQ", ORG)));   // duplicate room should still only subscribe once

        subscriber.reseedSubscriptions();

        verify(ch1, times(1)).subscribe(any(ClientSessionChannel.MessageListener.class));
        verify(ch2, times(1)).subscribe(any(ClientSessionChannel.MessageListener.class));
        assertThat(subscriber.getSubscribedRoomsSnapshot()).containsExactlyInAnyOrder("MCHQ", "WWWF");
    }

    @Test
    void reseedSubscriptions_skipsRoomsForDifferentOrganization() {
        ClientSessionChannel ch1 = mockChannel("MCHQ");
        when(userQueueRepository.findAll()).thenReturn(List.of(
                uq("MCHQ", ORG),
                uq("OTHR", "different-org")));

        subscriber.reseedSubscriptions();

        verify(ch1, times(1)).subscribe(any(ClientSessionChannel.MessageListener.class));
        assertThat(subscriber.getSubscribedRoomsSnapshot()).containsExactly("MCHQ");
    }

    @Test
    void extractRoomCode_parsesStandardChannel() {
        assertThat(FayeQueueSubscriber.extractRoomCode("/karaoke/" + ORG + "/rooms/MCHQ/queue"))
                .isEqualTo("MCHQ");
    }

    @Test
    void extractRoomCode_returnsNullForUnrelatedChannel() {
        assertThat(FayeQueueSubscriber.extractRoomCode("/meta/handshake")).isNull();
        assertThat(FayeQueueSubscriber.extractRoomCode(null)).isNull();
    }

    private ClientSessionChannel mockChannel(String room) {
        ClientSessionChannel ch = org.mockito.Mockito.mock(ClientSessionChannel.class);
        when(bayeuxClient.getChannel("/karaoke/" + ORG + "/rooms/" + room + "/queue")).thenReturn(ch);
        return ch;
    }

    private static UserQueue uq(String room, String org) {
        UserQueue uq = new UserQueue(room);
        uq.setOrganization(org);
        return uq;
    }
}
