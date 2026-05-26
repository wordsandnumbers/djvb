package com.vpo.djvoxbox.faye;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Faye payload for the {@code song_reorder} event on
 * {@code /karaoke/{orgId}/rooms/{roomCode}/queue}.
 *
 * <p>Describes a play that moved within the upstream queue. {@code oldIndex}
 * is the previous position; {@code index} is the new position. Both are
 * 0-based. The handler removes the play at {@code oldIndex} (validating its
 * {@code playId}) and reinserts at {@code index}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SongReorderEvent(
        @JsonProperty("event") String event,
        @JsonProperty("song_id") Integer songId,
        @JsonProperty("play_id") String playId,
        @JsonProperty("index") Integer index,
        @JsonProperty("old_index") Integer oldIndex) {
}
