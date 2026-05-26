package com.vpo.djvoxbox.faye;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Faye payload for the {@code song_update} event on
 * {@code /karaoke/{orgId}/rooms/{roomCode}/queue}.
 *
 * <p>Always describes the currently-playing song. A {@code playId} that
 * matches the cached {@code currentSong} indicates a position tick; a
 * {@code playId} that matches a play later in the queue indicates a
 * currentSong rollover (the previous song completed and the named play was
 * dequeued). A {@code playId} unknown to the snapshot means the snapshot is
 * stale and must be refetched from upstream.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SongUpdateEvent(
        @JsonProperty("event") String event,
        @JsonProperty("song_id") Integer songId,
        @JsonProperty("play_id") String playId,
        @JsonProperty("duration") Integer duration,
        @JsonProperty("position") Integer position,
        @JsonProperty("paused") Boolean paused,
        @JsonProperty("pitch_shift") Integer pitchShift,
        @JsonProperty("audio_channels") String audioChannels) {
}
