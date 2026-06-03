package com.vpo.djvoxbox.mcp;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.vpo.vbclient.feedback.FeedbackPrompt;
import com.vpo.vbclient.feedback.FeedbackResponse;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.model.Room;
import com.vpo.vbclient.model.ServiceCall;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.model.Song;
import com.vpo.vbclient.song.AutocompleteSuggestion;
import com.vpo.vbclient.song.LanguageList;
import com.vpo.vbclient.song.Search;
import com.vpo.vbclient.song.SongRequestResponse;
import com.vpo.vbclient.song.SongStats;
import com.vpo.vbclient.song.TagList;

@Component
public class VbsongsMcpTools {

    private final VbsongsOperations operations;

    public VbsongsMcpTools(VbsongsOperations operations) {
        this.operations = operations;
    }

    @Tool(name = "search_songs", description = "Search the vbsongs karaoke catalog.")
    public Search searchSongs(
            @ToolParam(description = "Search query.", required = true) String query,
            @ToolParam(description = "Song language. Defaults to the app default.", required = false) String language,
            @ToolParam(description = "Tag filter.", required = false) String tag,
            @ToolParam(description = "1-based result page.", required = false) Integer page,
            @ToolParam(description = "Results per page, 1 to 100.", required = false) Integer perPage,
            @ToolParam(description = "Include non-catalog or alternate results when supported.", required = false) Boolean includeOthers) {
        return operations.searchSongs(query, language, tag, page, perPage, includeOthers);
    }

    @Tool(name = "browse_songs", description = "Browse songs by artist, title, popularity, or recently added.")
    public Search browseSongs(
            @ToolParam(description = "Browse mode such as artist, title, popularity, or recently_added.", required = false) String by,
            @ToolParam(description = "Prefix filter.", required = false) String prefix,
            @ToolParam(description = "Song language. Defaults to the app default.", required = false) String language,
            @ToolParam(description = "Tag filter.", required = false) String tag,
            @ToolParam(description = "1-based result page.", required = false) Integer page,
            @ToolParam(description = "Results per page, 1 to 100.", required = false) Integer perPage,
            @ToolParam(description = "Only return songs with photos when supported.", required = false) Boolean onlyWithPhoto,
            @ToolParam(description = "Optional vbsongs session id.", required = false) String sessionId) {
        return operations.browseSongs(by, prefix, language, tag, page, perPage, onlyWithPhoto, sessionId);
    }

    @Tool(name = "get_song", description = "Get one song by numeric song id.")
    public Song getSong(@ToolParam(description = "Numeric song id.", required = true) Integer songId) {
        return operations.getSong(songId);
    }

    @Tool(name = "autocomplete_songs", description = "Get song-search autocomplete suggestions.")
    public List<AutocompleteSuggestion> autocompleteSongs(
            @ToolParam(description = "Partial search query.", required = true) String query,
            @ToolParam(description = "Song language.", required = false) String language,
            @ToolParam(description = "Maximum number of suggestions.", required = false) Integer limit) {
        return operations.autocompleteSongs(query, language, limit);
    }

    @Tool(name = "roulette_songs", description = "Return a random-ish roulette list of songs.")
    public List<Song> rouletteSongs(
            @ToolParam(description = "Song language. Defaults to the app default.", required = false) String language,
            @ToolParam(description = "Tag filter.", required = false) String tag,
            @ToolParam(description = "Starting popularity rank or offset.", required = false) Integer fromTop,
            @ToolParam(description = "Results per page, 1 to 100.", required = false) Integer perPage,
            @ToolParam(description = "Only return songs with photos when supported.", required = false) Boolean onlyWithPhoto,
            @ToolParam(description = "Optional vbsongs session id.", required = false) String sessionId) {
        return operations.rouletteSongs(language, tag, fromTop, perPage, onlyWithPhoto, sessionId);
    }

    @Tool(name = "list_languages", description = "List supported song languages.")
    public LanguageList listLanguages() {
        return operations.listLanguages();
    }

    @Tool(name = "list_tags", description = "List supported song tags.")
    public TagList listTags() {
        return operations.listTags();
    }

    @Tool(name = "get_song_stats", description = "Get catalog and play statistics.")
    public SongStats getSongStats() {
        return operations.getSongStats();
    }

    @Tool(name = "request_song", description = "Request that a missing song be added to the catalog.")
    public SongRequestResponse requestSong(
            @ToolParam(description = "Artist name.", required = true) String artist,
            @ToolParam(description = "Song title.", required = true) String title,
            @ToolParam(description = "Optional notes.", required = false) String notes,
            @ToolParam(description = "Optional vbsongs session id.", required = false) String sessionId) {
        return operations.requestSong(artist, title, notes, sessionId);
    }

    @Tool(name = "get_room", description = "Get room metadata by room code.")
    public Room getRoom(@ToolParam(description = "Room code.", required = true) String roomCode) {
        return operations.getRoom(roomCode);
    }

    @Tool(name = "get_queue", description = "Get the current queue for a room.")
    public Queue getQueue(@ToolParam(description = "Room code.", required = true) String roomCode) {
        return operations.getQueue(roomCode);
    }

    @Tool(name = "add_song_to_queue", description = "Add a song to a room queue.")
    public Play addSongToQueue(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Numeric song id.", required = true) Integer songId,
            @ToolParam(description = "Optional vbsongs session id.", required = false) String sessionId,
            @ToolParam(description = "Optional queue message.", required = false) String message,
            @ToolParam(description = "Optional message color, such as #1122FF.", required = false) String messageColor,
            @ToolParam(description = "Allow consecutive duplicate songs.", required = false) Boolean allowDuplicate,
            @ToolParam(description = "Optional destination play id or queue index.", required = false) String to) {
        return operations.addSongToQueue(roomCode, songId, sessionId, message, messageColor, allowDuplicate, to);
    }

    @Tool(name = "remove_play", description = "Remove a play from a room queue.")
    public void removePlay(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Play id to remove.", required = true) String playId) {
        operations.removePlay(roomCode, playId);
    }

    @Tool(name = "clear_queue", description = "Remove all queued songs from a room.")
    public void clearQueue(@ToolParam(description = "Room code.", required = true) String roomCode) {
        operations.clearQueue(roomCode);
    }

    @Tool(name = "reorder_queue", description = "Move a queued play from one position to another.")
    public void reorderQueue(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Source play id or queue index.", required = true) String from,
            @ToolParam(description = "Destination play id or queue index.", required = true) String to) {
        operations.reorderQueue(roomCode, from, to);
    }

    @Tool(name = "replace_play", description = "Replace an existing queue play with another song.")
    public Play replacePlay(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Current play id to replace.", required = true) String currentPlayId,
            @ToolParam(description = "Replacement numeric song id.", required = true) Integer replacementSongId,
            @ToolParam(description = "Optional vbsongs session id.", required = false) String sessionId,
            @ToolParam(description = "Optional queue message.", required = false) String message,
            @ToolParam(description = "Optional message color, such as #1122FF.", required = false) String messageColor,
            @ToolParam(description = "Allow consecutive duplicate songs.", required = false) Boolean allowDuplicate) {
        return operations.replacePlay(roomCode, currentPlayId, replacementSongId, sessionId, message, messageColor,
                allowDuplicate);
    }

    @Tool(name = "skip_current_song", description = "Skip the current song in a room.")
    public void skipCurrentSong(@ToolParam(description = "Room code.", required = true) String roomCode) {
        operations.skipCurrentSong(roomCode);
    }

    @Tool(name = "restart_current_song", description = "Restart the current song in a room.")
    public void restartCurrentSong(@ToolParam(description = "Room code.", required = true) String roomCode) {
        operations.restartCurrentSong(roomCode);
    }

    @Tool(name = "pause_current_song", description = "Pause the current song in a room.")
    public void pauseCurrentSong(@ToolParam(description = "Room code.", required = true) String roomCode) {
        operations.pauseCurrentSong(roomCode);
    }

    @Tool(name = "resume_current_song", description = "Resume the current song in a room.")
    public void resumeCurrentSong(@ToolParam(description = "Room code.", required = true) String roomCode) {
        operations.resumeCurrentSong(roomCode);
    }

    @Tool(name = "set_audio", description = "Set room audio values.")
    public void setAudio(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Volume, 0 to 100.", required = false) Integer volume,
            @ToolParam(description = "Pitch shift, -12 to 12.", required = false) Integer pitchShift,
            @ToolParam(description = "Audio channels value supported by vbsongs.", required = false) String channels) {
        operations.setAudio(roomCode, volume, pitchShift, channels);
    }

    @Tool(name = "set_lights", description = "Set room light mode/effects.")
    public void setLights(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Light mode.", required = true) Integer mode,
            @ToolParam(description = "Light effects value. Defaults to 1.", required = false) Integer effects) {
        operations.setLights(roomCode, mode, effects);
    }

    @Tool(name = "post_popup", description = "Post a popup message to a room.")
    public void postPopup(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "vbsongs session id.", required = true) String sessionId,
            @ToolParam(description = "Popup text.", required = true) String text) {
        operations.postPopup(roomCode, sessionId, text);
    }

    @Tool(name = "login", description = "Create or refresh a vbsongs session.")
    public Session login(
            @ToolParam(description = "User email.", required = false) String email,
            @ToolParam(description = "Display handle.", required = false) String handle,
            @ToolParam(description = "Profile color.", required = false) String color,
            @ToolParam(description = "Existing session id to refresh.", required = false) String existingSessionId) {
        return operations.login(email, handle, color, existingSessionId);
    }

    @Tool(name = "get_profile", description = "Get a vbsongs profile by session id.")
    public Session getProfile(@ToolParam(description = "vbsongs session id.", required = true) String sessionId) {
        return operations.getProfile(sessionId);
    }

    @Tool(name = "update_profile", description = "Update a vbsongs profile.")
    public Session updateProfile(
            @ToolParam(description = "vbsongs session id.", required = true) String sessionId,
            @ToolParam(description = "User email.", required = false) String email,
            @ToolParam(description = "Display handle.", required = false) String handle,
            @ToolParam(description = "Profile color.", required = false) String color,
            @ToolParam(description = "Hide handle in queue.", required = false) Boolean hideHandle,
            @ToolParam(description = "Birth year.", required = false) Integer birthYear,
            @ToolParam(description = "Birth month, 1 to 12.", required = false) Integer birthMonth,
            @ToolParam(description = "Birth day, 1 to 31.", required = false) Integer birthDay,
            @ToolParam(description = "Zip code.", required = false) String zipCode,
            @ToolParam(description = "Prompt for handle on every play.", required = false) Boolean promptForHandleOnEveryPlay) {
        return operations.updateProfile(sessionId, email, handle, color, hideHandle, birthYear, birthMonth, birthDay,
                zipCode, promptForHandleOnEveryPlay);
    }

    @Tool(name = "get_service_call", description = "Get the room service-call state.")
    public ServiceCall getServiceCall(@ToolParam(description = "Room code.", required = true) String roomCode) {
        return operations.getServiceCall(roomCode);
    }

    @Tool(name = "set_service_call", description = "Set the room service-call state.")
    public void setServiceCall(
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "vbsongs session id.", required = true) String sessionId,
            @ToolParam(description = "State: requested, cancelled, rerequested, acknowledged, served, reset, or none.", required = true) String state) {
        operations.setServiceCall(roomCode, sessionId, state);
    }

    @Tool(name = "get_play_history", description = "Get play history for a vbsongs session.")
    public Search getPlayHistory(
            @ToolParam(description = "vbsongs session id.", required = true) String sessionId,
            @ToolParam(description = "1-based result page.", required = false) Integer page,
            @ToolParam(description = "Results per page, 1 to 100.", required = false) Integer perPage) {
        return operations.getPlayHistory(sessionId, page, perPage);
    }

    @Tool(name = "get_feedback_prompts", description = "Get feedback prompts for a room/session.")
    public List<FeedbackPrompt> getFeedbackPrompts(
            @ToolParam(description = "vbsongs session id.", required = true) String sessionId,
            @ToolParam(description = "Room code.", required = true) String roomCode) {
        return operations.getFeedbackPrompts(sessionId, roomCode);
    }

    @Tool(name = "submit_feedback", description = "Submit feedback responses for a room/session.")
    public void submitFeedback(
            @ToolParam(description = "vbsongs session id.", required = true) String sessionId,
            @ToolParam(description = "Room code.", required = true) String roomCode,
            @ToolParam(description = "Feedback responses with guid, rating, and optional notes.", required = true) List<FeedbackResponse> responses) {
        operations.submitFeedback(sessionId, roomCode, responses);
    }
}
