package com.vpo.djvoxbox.mcp;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.vpo.djvoxbox.faye.QueueSnapshotStore;
import com.vpo.vbclient.currentsong.CurrentSongClient;
import com.vpo.vbclient.feedback.FeedbackClient;
import com.vpo.vbclient.feedback.FeedbackPrompt;
import com.vpo.vbclient.feedback.FeedbackResponse;
import com.vpo.vbclient.model.Audio;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.model.Room;
import com.vpo.vbclient.model.ServiceCall;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.model.Song;
import com.vpo.vbclient.queue.PlayRequest;
import com.vpo.vbclient.queue.QueueClient;
import com.vpo.vbclient.room.RoomClient;
import com.vpo.vbclient.session.SessionClient;
import com.vpo.vbclient.song.AutocompleteSuggestion;
import com.vpo.vbclient.song.LanguageList;
import com.vpo.vbclient.song.RouletteRequest;
import com.vpo.vbclient.song.Search;
import com.vpo.vbclient.song.SongClient;
import com.vpo.vbclient.song.SongRequest;
import com.vpo.vbclient.song.SongRequestResponse;
import com.vpo.vbclient.song.SongStats;
import com.vpo.vbclient.song.TagList;

@Service
public class VbsongsOperations {

    private static final int MAX_PER_PAGE = 100;
    private static final Set<String> SERVICE_CALL_STATES = Set.of(
            ServiceCall.STATE_REQUESTED,
            ServiceCall.STATE_CANCELLED,
            ServiceCall.STATE_REREQUESTED,
            ServiceCall.STATE_ACKNOWLEDGED,
            ServiceCall.STATE_SERVED,
            ServiceCall.STATE_RESET,
            ServiceCall.STATE_NONE);

    private final SongClient songClient;
    private final QueueClient queueClient;
    private final SessionClient sessionClient;
    private final RoomClient roomClient;
    private final CurrentSongClient currentSongClient;
    private final FeedbackClient feedbackClient;
    private final QueueSnapshotStore queueSnapshotStore;
    private final String defaultLanguage;

    public VbsongsOperations(
            SongClient songClient,
            QueueClient queueClient,
            SessionClient sessionClient,
            RoomClient roomClient,
            CurrentSongClient currentSongClient,
            FeedbackClient feedbackClient,
            QueueSnapshotStore queueSnapshotStore,
            @Value("${default.language:English}") String defaultLanguage) {
        this.songClient = songClient;
        this.queueClient = queueClient;
        this.sessionClient = sessionClient;
        this.roomClient = roomClient;
        this.currentSongClient = currentSongClient;
        this.feedbackClient = feedbackClient;
        this.queueSnapshotStore = queueSnapshotStore;
        this.defaultLanguage = defaultLanguage;
    }

    public Search searchSongs(String query, String language, String tag, Integer page, Integer perPage,
            Boolean includeOthers) {
        return upstream("search songs", () -> {
            Search search = pagedSearch(language, tag, page, perPage);
            search.setQuery(requireText(query, "query"));
            search.setIncludeOthers(includeOthers);
            return songClient.findSongs(search);
        });
    }

    public Search browseSongs(String by, String prefix, String language, String tag, Integer page, Integer perPage,
            Boolean onlyWithPhoto, String sessionId) {
        return upstream("browse songs", () -> {
            Search search = pagedSearch(language, tag, page, perPage);
            search.setBrowse(true);
            search.setBy(by);
            search.setQuery(prefix);
            search.setOnlyWithPhoto(onlyWithPhoto);
            search.setSession(optionalSession(sessionId));
            return songClient.findSongs(search);
        });
    }

    public Song getSong(Integer songId) {
        return upstream("get song", () -> songClient.getSongById(requirePositive(songId, "songId")));
    }

    public List<AutocompleteSuggestion> autocompleteSongs(String query, String language, Integer limit) {
        return upstream("autocomplete songs", () -> {
            if (limit != null) {
                requireRange(limit, "limit", 1, MAX_PER_PAGE);
            }
            return songClient.autocomplete(requireText(query, "query"), blankToNull(language), limit);
        });
    }

    public List<Song> rouletteSongs(String language, String tag, Integer fromTop, Integer perPage,
            Boolean onlyWithPhoto, String sessionId) {
        return upstream("roulette songs", () -> {
            if (fromTop != null) {
                requireRange(fromTop, "fromTop", 0, 1000);
            }
            if (perPage != null) {
                requireRange(perPage, "perPage", 1, MAX_PER_PAGE);
            }
            RouletteRequest request = new RouletteRequest();
            request.setLanguage(languageOrDefault(language));
            request.setTag(blankToNull(tag));
            request.setFromTop(fromTop);
            request.setPerPage(perPage);
            request.setOnlyWithPhoto(onlyWithPhoto);
            request.setSession(optionalSession(sessionId));
            return songClient.roulette(request);
        });
    }

    public LanguageList listLanguages() {
        return upstream("list languages", songClient::languages);
    }

    public TagList listTags() {
        return upstream("list tags", songClient::tags);
    }

    public SongStats getSongStats() {
        return upstream("get song stats", songClient::stats);
    }

    public SongRequestResponse requestSong(String artist, String title, String notes, String sessionId) {
        return upstream("request song", () -> {
            SongRequest request = new SongRequest(requireText(artist, "artist"), requireText(title, "title"));
            request.setNotes(blankToNull(notes));
            return songClient.requestSong(request, optionalSession(sessionId));
        });
    }

    public Room getRoom(String roomCode) {
        return upstream("get room", () -> roomClient.getRoom(requireRoomCode(roomCode)));
    }

    public Queue getQueue(String roomCode) {
        return upstream("get queue", () -> queueClient.getQueue(requireRoomCode(roomCode)));
    }

    public Play addSongToQueue(String roomCode, Integer songId, String sessionId, String message,
            String messageColor, Boolean allowDuplicate, String to) {
        return upstream("add song to queue", () -> {
            String code = requireRoomCode(roomCode);
            PlayRequest request = new PlayRequest(code, requirePositive(songId, "songId"));
            request.setMessage(blankToNull(message));
            request.setMessageColor(blankToNull(messageColor));
            request.setAllowDuplicate(Boolean.TRUE.equals(allowDuplicate));
            request.setTo(blankToNull(to));
            Play play = queueClient.addSong(request, optionalSession(sessionId));
            evict(code);
            return play;
        });
    }

    public void removePlay(String roomCode, String playId) {
        upstream("remove play", () -> {
            String code = requireRoomCode(roomCode);
            Play play = new Play();
            play.setPlayId(requireText(playId, "playId"));
            queueClient.deletePlay(code, play);
            evict(code);
            return null;
        });
    }

    public void clearQueue(String roomCode) {
        upstream("clear queue", () -> {
            String code = requireRoomCode(roomCode);
            queueClient.deleteAll(code);
            evict(code);
            return null;
        });
    }

    public void reorderQueue(String roomCode, String from, String to) {
        upstream("reorder queue", () -> {
            String code = requireRoomCode(roomCode);
            queueClient.reorder(code, requireText(from, "from"), requireText(to, "to"));
            evict(code);
            return null;
        });
    }

    public Play replacePlay(String roomCode, String currentPlayId, Integer replacementSongId, String sessionId,
            String message, String messageColor, Boolean allowDuplicate) {
        return upstream("replace play", () -> {
            String code = requireRoomCode(roomCode);
            Play current = new Play();
            current.setPlayId(requireText(currentPlayId, "currentPlayId"));
            PlayRequest replacement = new PlayRequest(code, requirePositive(replacementSongId, "replacementSongId"));
            replacement.setMessage(blankToNull(message));
            replacement.setMessageColor(blankToNull(messageColor));
            replacement.setAllowDuplicate(Boolean.TRUE.equals(allowDuplicate));
            Play play = queueClient.replace(code, current, replacement, optionalSession(sessionId));
            evict(code);
            return play;
        });
    }

    public void skipCurrentSong(String roomCode) {
        currentSongAction("skip current song", roomCode, currentSongClient::skip);
    }

    public void restartCurrentSong(String roomCode) {
        currentSongAction("restart current song", roomCode, currentSongClient::restart);
    }

    public void pauseCurrentSong(String roomCode) {
        currentSongAction("pause current song", roomCode, currentSongClient::pause);
    }

    public void resumeCurrentSong(String roomCode) {
        currentSongAction("resume current song", roomCode, currentSongClient::resume);
    }

    public void setAudio(String roomCode, Integer volume, Integer pitchShift, String channels) {
        upstream("set audio", () -> {
            String code = requireRoomCode(roomCode);
            Audio audio = new Audio(code);
            if (volume != null) {
                audio.setVolume(requireRange(volume, "volume", 0, 100));
            }
            if (pitchShift != null) {
                audio.setPitchShift(requireRange(pitchShift, "pitchShift", -12, 12));
            }
            audio.setChannels(blankToNull(channels));
            currentSongClient.setAudio(audio);
            return null;
        });
    }

    public void setLights(String roomCode, Integer mode, Integer effects) {
        upstream("set lights", () -> {
            sessionClient.controlLights(requireRoomCode(roomCode),
                    requireRange(mode, "mode", 0, 100),
                    effects == null ? 1 : requireRange(effects, "effects", 0, 100));
            return null;
        });
    }

    public void postPopup(String roomCode, String sessionId, String text) {
        upstream("post popup", () -> {
            sessionClient.postPopup(requireSession(sessionId), requireRoomCode(roomCode), requireText(text, "text"));
            return null;
        });
    }

    public Session login(String email, String handle, String color, String existingSessionId) {
        return upstream("login", () -> {
            Session session = new Session();
            session.setEmail(blankToNull(email));
            session.setHandle(blankToNull(handle));
            session.setColor(blankToNull(color));
            if (StringUtils.hasText(existingSessionId)) {
                return sessionClient.createSession(session, existingSessionId);
            }
            return sessionClient.createSession(session);
        });
    }

    public Session getProfile(String sessionId) {
        return upstream("get profile", () -> sessionClient.getSessionById(requireText(sessionId, "sessionId")));
    }

    public Session updateProfile(String sessionId, String email, String handle, String color, Boolean hideHandle,
            Integer birthYear, Integer birthMonth, Integer birthDay, String zipCode,
            Boolean promptForHandleOnEveryPlay) {
        return upstream("update profile", () -> {
            Session session = requireSession(sessionId);
            session.setEmail(blankToNull(email));
            session.setHandle(blankToNull(handle));
            session.setColor(blankToNull(color));
            if (hideHandle != null) {
                session.setHideHandle(hideHandle);
            }
            if (birthYear != null) {
                session.setBirthYear(requireRange(birthYear, "birthYear", 1900, 2100));
            }
            if (birthMonth != null) {
                session.setBirthMonth(requireRange(birthMonth, "birthMonth", 1, 12));
            }
            if (birthDay != null) {
                session.setBirthDay(requireRange(birthDay, "birthDay", 1, 31));
            }
            session.setZipCode(blankToNull(zipCode));
            session.setPromptForHandleOnEveryPlay(promptForHandleOnEveryPlay);
            return sessionClient.updateSession(session);
        });
    }

    public ServiceCall getServiceCall(String roomCode) {
        return upstream("get service call", () -> roomClient.getServiceCall(requireRoomCode(roomCode)));
    }

    public void setServiceCall(String roomCode, String sessionId, String state) {
        upstream("set service call", () -> {
            String requestedState = requireText(state, "state");
            if (!SERVICE_CALL_STATES.contains(requestedState)) {
                throw new IllegalArgumentException("state must be one of " + SERVICE_CALL_STATES);
            }
            roomClient.setServiceCall(requireSession(sessionId), requireRoomCode(roomCode), requestedState);
            return null;
        });
    }

    public Search getPlayHistory(String sessionId, Integer page, Integer perPage) {
        return upstream("get play history", () -> {
            Search search = pagedSearch(null, null, page, perPage);
            search.setPlayHistory(true);
            search.setSession(requireSession(sessionId));
            return songClient.playHistory(search);
        });
    }

    public List<FeedbackPrompt> getFeedbackPrompts(String sessionId, String roomCode) {
        return upstream("get feedback prompts",
                () -> feedbackClient.getPrompts(requireSession(sessionId), requireRoomCode(roomCode)));
    }

    public void submitFeedback(String sessionId, String roomCode, List<FeedbackResponse> responses) {
        upstream("submit feedback", () -> {
            if (responses == null || responses.isEmpty()) {
                throw new IllegalArgumentException("responses must not be empty");
            }
            feedbackClient.submit(requireSession(sessionId), requireRoomCode(roomCode), responses);
            return null;
        });
    }

    private Search pagedSearch(String language, String tag, Integer page, Integer perPage) {
        Search search = new Search();
        search.setLanguage(languageOrDefault(language));
        search.setTag(blankToNull(tag));
        search.setPage(page == null ? 1 : requireRange(page, "page", 1, 10000));
        search.setPerPage(perPage == null ? 50 : requireRange(perPage, "perPage", 1, MAX_PER_PAGE));
        return search;
    }

    private void currentSongAction(String action, String roomCode, RoomAction roomAction) {
        upstream(action, () -> {
            String code = requireRoomCode(roomCode);
            roomAction.apply(code);
            evict(code);
            return null;
        });
    }

    private void evict(String roomCode) {
        queueSnapshotStore.evict(roomCode);
    }

    private String requireRoomCode(String roomCode) {
        String value = requireText(roomCode, "roomCode");
        if (value.length() < 3 || value.length() > 16) {
            throw new IllegalArgumentException("roomCode must be between 3 and 16 characters");
        }
        return value;
    }

    private Session requireSession(String sessionId) {
        return new Session(requireText(sessionId, "sessionId"));
    }

    private Session optionalSession(String sessionId) {
        return StringUtils.hasText(sessionId) ? new Session(sessionId) : null;
    }

    private String requireText(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }

    private Integer requirePositive(Integer value, String name) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private Integer requireRange(Integer value, String name, int min, int max) {
        if (value == null || value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }

    private String languageOrDefault(String language) {
        return StringUtils.hasText(language) ? language.trim() : defaultLanguage;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> T upstream(String action, UpstreamCall<T> call) {
        try {
            return call.execute();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Unable to " + action + ": upstream returned "
                    + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("Unable to " + action + ": upstream request failed", e);
        }
    }

    @FunctionalInterface
    private interface UpstreamCall<T> {
        T execute();
    }

    @FunctionalInterface
    private interface RoomAction {
        void apply(String roomCode);
    }
}
