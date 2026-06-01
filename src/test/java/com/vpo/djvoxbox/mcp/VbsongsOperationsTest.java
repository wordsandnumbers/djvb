package com.vpo.djvoxbox.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vpo.djvoxbox.faye.QueueSnapshotStore;
import com.vpo.vbclient.currentsong.CurrentSongClient;
import com.vpo.vbclient.feedback.FeedbackClient;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.queue.PlayRequest;
import com.vpo.vbclient.queue.QueueClient;
import com.vpo.vbclient.room.RoomClient;
import com.vpo.vbclient.session.SessionClient;
import com.vpo.vbclient.song.Search;
import com.vpo.vbclient.song.SongClient;

@ExtendWith(MockitoExtension.class)
class VbsongsOperationsTest {

    @Mock SongClient songClient;
    @Mock QueueClient queueClient;
    @Mock SessionClient sessionClient;
    @Mock RoomClient roomClient;
    @Mock CurrentSongClient currentSongClient;
    @Mock FeedbackClient feedbackClient;
    @Mock QueueSnapshotStore queueSnapshotStore;

    private VbsongsOperations operations;

    @BeforeEach
    void setUp() {
        operations = new VbsongsOperations(songClient, queueClient, sessionClient, roomClient,
                currentSongClient, feedbackClient, queueSnapshotStore, "English");
    }

    @Test
    void searchSongs_buildsDefaultPagedSearch() {
        Search result = new Search();
        when(songClient.findSongs(any(Search.class))).thenReturn(result);

        Search actual = operations.searchSongs("bowie", null, "duet", null, null, true);

        assertThat(actual).isSameAs(result);
        ArgumentCaptor<Search> captor = ArgumentCaptor.forClass(Search.class);
        verify(songClient).findSongs(captor.capture());
        Search search = captor.getValue();
        assertThat(search.getQuery()).isEqualTo("bowie");
        assertThat(search.getLanguage()).isEqualTo("English");
        assertThat(search.getTag()).isEqualTo("duet");
        assertThat(search.getPage()).isEqualTo(1);
        assertThat(search.getPerPage()).isEqualTo(50);
        assertThat(search.getIncludeOthers()).isTrue();
    }

    @Test
    void addSongToQueue_delegatesAndEvictsSnapshot() {
        Play play = new Play();
        play.setPlayId("p1");
        when(queueClient.addSong(any(PlayRequest.class), any(Session.class))).thenReturn(play);

        Play actual = operations.addSongToQueue("MCHQ", 123, "sess-1", "Justin", "#1122FF", true, "-1");

        assertThat(actual).isSameAs(play);
        ArgumentCaptor<PlayRequest> requestCaptor = ArgumentCaptor.forClass(PlayRequest.class);
        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(queueClient).addSong(requestCaptor.capture(), sessionCaptor.capture());
        PlayRequest request = requestCaptor.getValue();
        assertThat(request.getRoomCode()).isEqualTo("MCHQ");
        assertThat(request.getSongId()).isEqualTo(123);
        assertThat(request.getMessage()).isEqualTo("Justin");
        assertThat(request.getMessageColor()).isEqualTo("#1122FF");
        assertThat(request.isAllowDuplicate()).isTrue();
        assertThat(request.getTo()).isEqualTo("-1");
        assertThat(sessionCaptor.getValue().getSession()).isEqualTo("sess-1");
        verify(queueSnapshotStore).evict("MCHQ");
    }

    @Test
    void currentSongMutationEvictsSnapshot() {
        operations.skipCurrentSong("MCHQ");

        verify(currentSongClient).skip("MCHQ");
        verify(queueSnapshotStore).evict("MCHQ");
    }

    @Test
    void invalidSongIdDoesNotCallUpstream() {
        assertThatThrownBy(() -> operations.getSong(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("songId");

        verify(songClient, never()).getSongById(any());
    }

    @Test
    void invalidServiceCallStateDoesNotCallUpstream() {
        assertThatThrownBy(() -> operations.setServiceCall("MCHQ", "sess-1", "wat"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("state");

        verify(roomClient, never()).setServiceCall(any(), eq("MCHQ"), any());
    }
}
