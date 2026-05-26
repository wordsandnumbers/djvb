package com.vpo.djvoxbox.faye;

import java.util.Map;

/**
 * Small, serializable record handed from the notify phase of
 * {@link QueueEventHandler} to a {@link QueueEventBroadcaster}.
 *
 * <p>It deliberately omits anything caller-specific: any subset of UserQueues
 * for the same {@code roomCode} can derive their per-row UI state from
 * {@code estimatedPlayTimeByPlayId} (keyed by Play.playId). The fan-out layer
 * (e.g. an SSE registry) is expected to filter/route by room.
 *
 * @param roomCode                   karaoke room this update applies to.
 * @param currentPlayId              {@code playId} of the song that is now
 *                                   playing (post-mutation).
 * @param currentPosition            position in ms of the currently-playing
 *                                   song, or {@code null} if not advanced by
 *                                   this event.
 * @param paused                     whether the upstream playback is paused,
 *                                   or {@code null} if not advanced by this
 *                                   event.
 * @param currentSongRolledOver      {@code true} when this event caused the
 *                                   currently-playing song to change; SSE
 *                                   consumers may use this as a "next song
 *                                   started" hint.
 * @param estimatedPlayTimeByPlayId  derived from {@link com.vpo.djvoxbox.app.ConvenientQueue};
 *                                   covers the new currentSong and every play
 *                                   remaining in the upstream queue.
 */
public record QueueViewPayload(
        String roomCode,
        String currentPlayId,
        Integer currentPosition,
        Boolean paused,
        boolean currentSongRolledOver,
        Map<String, Long> estimatedPlayTimeByPlayId) {
}
