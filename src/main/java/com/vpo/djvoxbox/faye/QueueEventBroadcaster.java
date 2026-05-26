package com.vpo.djvoxbox.faye;

/**
 * Sink for the notify phase of {@link QueueEventHandler}.
 *
 * <p>This PR introduces only the seam; the real implementation arrives with
 * the SSE follow-up, which will replace {@link NoopQueueEventBroadcaster} with
 * an {@code SseEmitterRegistry}-backed component that fans the payload out to
 * connected browsers subscribed to {@code roomCode}.
 *
 * <p>The notify phase calls {@link #broadcast} on every instance for every
 * event, independent of the Manager event-lock. Implementations must therefore
 * be cheap and non-blocking; downstream errors should be logged and swallowed
 * so a misbehaving listener cannot break the upstream-mirror invariant.
 */
public interface QueueEventBroadcaster {

    /**
     * Hand the post-mutation view to the broadcaster. May be invoked
     * concurrently from multiple Faye listener threads (one per channel).
     */
    void broadcast(String roomCode, QueueViewPayload payload);
}
