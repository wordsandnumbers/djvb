package com.vpo.djvoxbox.app;

import java.util.HashMap;
import java.util.Map;

import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;

/**
 * Read-only projection over an upstream {@link Queue} snapshot that derives an
 * "estimated wall-clock time at which each play is expected to start".
 *
 * <p>The map keys are {@code playId}s (covering the currently-playing song and
 * every play in the queue), the values are millisecond epoch timestamps. The
 * derivation walks the queue in order, accumulating durations onto a running
 * clock that begins at {@link System#currentTimeMillis()} adjusted for the
 * remaining time on the currently-playing song. A null duration falls back to
 * a 4-minute (240_000ms) heuristic so a single mis-tagged play does not
 * collapse all downstream estimates.
 *
 * <p>Extracted from a previously-private inner class on
 * {@link QueueManagementService} so the WS event path
 * ({@code com.vpo.djvoxbox.faye.QueueEventHandler}) can reuse the same
 * derivation against a Redis-cached snapshot without depending on Spring beans.
 *
 * <p>Threading: instances are not thread-safe; create one per event.
 */
public class ConvenientQueue {

    private final Queue queue;
    private final Map<String, Long> playData = new HashMap<>();

    /**
     * @param queue upstream snapshot to derive from; may be {@code null} (in
     *              which case {@link #getPlayData()} returns an empty map).
     */
    public ConvenientQueue(Queue queue) {
        this.queue = queue;
        if (queue != null) {
            extractPlayIds();
        }
    }

    private void extractPlayIds() {
        Long time = System.currentTimeMillis();
        if (queue != null && queue.getCurrentSong() != null) {
            this.playData.put(queue.getCurrentSong().getPlayId(), time);
            int playTime = 0;
            if (queue.getCurrentSong().getDuration() != null
                    && queue.getCurrentSong().getPosition() != null) {
                playTime = queue.getCurrentSong().getDuration() - queue.getCurrentSong().getPosition();
            }
            time = makeTime(time, playTime);
        }
        if (queue != null && queue.getQueue() != null) {
            for (Play play : queue.getQueue()) {
                this.playData.put(play.getPlayId(), time);
                time = makeTime(time, play.getDuration());
            }
        }
    }

    private Long makeTime(Long time, Integer add) {
        return time + ((add != null) ? add.longValue() : 240000L);
    }

    public Queue getQueue() {
        return queue;
    }

    public Map<String, Long> getPlayData() {
        return playData;
    }
}
