package com.vpo.djvoxbox.faye;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default no-op {@link QueueEventBroadcaster} wired in this PR. Logs at TRACE
 * so the seam is observable in development without flooding logs in
 * production. The SSE follow-up will swap this implementation out (either by
 * removing the {@code @Component} or by adding {@code @Primary} on a new bean).
 */
@Component
public class NoopQueueEventBroadcaster implements QueueEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(NoopQueueEventBroadcaster.class);

    @Override
    public void broadcast(String roomCode, QueueViewPayload payload) {
        if (log.isTraceEnabled()) {
            log.trace("noop broadcast room={} currentPlayId={} rolledOver={}",
                    roomCode, payload.currentPlayId(), payload.currentSongRolledOver());
        }
    }
}
