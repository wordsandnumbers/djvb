package com.vpo.djvoxbox.faye;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpo.djvoxbox.app.QueueManagementService;
import com.vpo.djvoxbox.domain.Manager;
import com.vpo.djvoxbox.domain.ManagerRepository;
import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.queue.QueueClient;

/**
 * Verifies the two-phase pattern in {@link QueueEventHandler}:
 *
 * <ul>
 *   <li>Notify phase mutates the snapshot, computes the view, and broadcasts
 *       on every event regardless of who holds the Manager event-lock.</li>
 *   <li>Persist+automate phase only runs on the lock-holder; non-holders
 *       skip {@code save} and {@code handleCurrentSongChanged}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class QueueEventHandlerTest {

    private static final String ROOM = "MCHQ";
    private static final String MANAGER_NAME = "test-manager";

    @Mock QueueSnapshotStore snapshotStore;
    @Mock UserQueueRepository userQueueRepository;
    @Mock ManagerRepository managerRepository;
    @Mock QueueClient queueClient;
    @Mock QueueEventBroadcaster broadcaster;
    @Mock QueueManagementService queueManagementService;

    private QueueEventHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new QueueEventHandler(
                snapshotStore,
                userQueueRepository,
                managerRepository,
                queueClient,
                broadcaster,
                queueManagementService,
                objectMapper);
        ReflectionTestUtils.setField(handler, "managerName", MANAGER_NAME);
        ReflectionTestUtils.setField(handler, "eventLockTtlMs", 15000L);
    }

    @Test
    void songUpdate_positionAdvance_updatesSnapshotAndBroadcasts() {
        Queue snapshot = queue(play("cur", 100, 30000, 0), List.of(play("p1", 101, 60000, 0)));
        when(snapshotStore.get(ROOM)).thenReturn(snapshot);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(null);

        Map<String, Object> raw = updateEvent("cur", 12345);
        handler.notify(ROOM, raw);

        ArgumentCaptor<Queue> saved = ArgumentCaptor.forClass(Queue.class);
        verify(snapshotStore).put(eq(ROOM), saved.capture());
        assertThat(saved.getValue().getCurrentSong().getPosition()).isEqualTo(12345);

        ArgumentCaptor<QueueViewPayload> payload = ArgumentCaptor.forClass(QueueViewPayload.class);
        verify(broadcaster).broadcast(eq(ROOM), payload.capture());
        assertThat(payload.getValue().currentPlayId()).isEqualTo("cur");
        assertThat(payload.getValue().currentSongRolledOver()).isFalse();
        assertThat(payload.getValue().currentPosition()).isEqualTo(12345);
    }

    @Test
    void songUpdate_currentSongRollover_popsQueueAndBroadcasts() {
        Queue snapshot = queue(
                play("cur", 100, 30000, 30000),
                List.of(play("p1", 101, 60000, 0), play("p2", 102, 60000, 0)));
        when(snapshotStore.get(ROOM)).thenReturn(snapshot);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(null);

        // event identifies p1 (a queued play) as now-playing
        handler.notify(ROOM, updateEvent("p1", 500));

        ArgumentCaptor<Queue> saved = ArgumentCaptor.forClass(Queue.class);
        verify(snapshotStore).put(eq(ROOM), saved.capture());
        Queue post = saved.getValue();
        assertThat(post.getCurrentSong().getPlayId()).isEqualTo("p1");
        assertThat(post.getCurrentSong().getPosition()).isEqualTo(500);
        // p1 was at index 0 → all of [0..0] removed; queue should now be [p2]
        assertThat(post.getQueue()).hasSize(1);
        assertThat(post.getQueue().get(0).getPlayId()).isEqualTo("p2");

        ArgumentCaptor<QueueViewPayload> payload = ArgumentCaptor.forClass(QueueViewPayload.class);
        verify(broadcaster).broadcast(eq(ROOM), payload.capture());
        assertThat(payload.getValue().currentSongRolledOver()).isTrue();
    }

    @Test
    void songUpdate_unknownPlayId_evictsAndReseeds() {
        Queue stale = queue(play("cur", 100, 30000, 0), List.of(play("p1", 101, 60000, 0)));
        Queue fresh = queue(play("freshCur", 999, 30000, 0), List.of());
        when(snapshotStore.get(ROOM)).thenReturn(stale);
        when(queueClient.getQueue(ROOM)).thenReturn(fresh);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(null);

        // event references an unknown playId
        handler.notify(ROOM, updateEvent("ghost", 0));

        verify(queueClient).getQueue(ROOM);
        verify(snapshotStore).put(eq(ROOM), eq(fresh));
        verify(broadcaster).broadcast(eq(ROOM), any(QueueViewPayload.class));
    }

    @Test
    void notifyAlwaysRunsEvenWhenLockNotAcquired() {
        Queue snapshot = queue(play("cur", 100, 30000, 0), new ArrayList<>());
        when(snapshotStore.get(ROOM)).thenReturn(snapshot);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(null);

        handler.notify(ROOM, updateEvent("cur", 1000));

        verify(broadcaster, times(1)).broadcast(eq(ROOM), any(QueueViewPayload.class));
        verify(userQueueRepository, never()).save(any());
        verify(queueManagementService, never()).handleCurrentSongChanged(any());
    }

    @Test
    void persistAndAutomate_onRollover_callsHandleCurrentSongChanged() {
        Queue snapshot = queue(
                play("cur", 100, 30000, 30000),
                new ArrayList<>(List.of(play("p1", 101, 60000, 0))));
        when(snapshotStore.get(ROOM)).thenReturn(snapshot);
        Manager held = new Manager();
        held.setName(MANAGER_NAME);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(held);
        UserQueue uq = userQueueWith("p1");
        when(userQueueRepository.findByRoomCode(ROOM)).thenReturn(List.of(uq));

        handler.notify(ROOM, updateEvent("p1", 0));

        verify(userQueueRepository).save(uq);
        verify(queueManagementService).handleCurrentSongChanged(ROOM);
        verify(managerRepository).releaseEventLock(held);
    }

    @Test
    void persistAndAutomate_noRollover_doesNotInvokeAutomation() {
        UserQueue uq = userQueueWith("cur");
        Queue snapshot = queue(play("cur", 100, 30000, 0), new ArrayList<>());
        when(snapshotStore.get(ROOM)).thenReturn(snapshot);
        Manager held = new Manager();
        held.setName(MANAGER_NAME);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(held);
        when(userQueueRepository.findByRoomCode(ROOM)).thenReturn(List.of(uq));

        handler.notify(ROOM, updateEvent("cur", 5000));

        verify(userQueueRepository).save(uq);
        verify(queueManagementService, never()).handleCurrentSongChanged(any());
        verify(managerRepository).releaseEventLock(held);
    }

    @Test
    void songReorder_validOldIndex_movesPlayInSnapshot() {
        Queue snapshot = queue(
                play("cur", 100, 30000, 0),
                new ArrayList<>(List.of(play("p1", 101, 60000, 0), play("p2", 102, 60000, 0), play("p3", 103, 60000, 0))));
        when(snapshotStore.get(ROOM)).thenReturn(snapshot);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(null);

        handler.notify(ROOM, reorderEvent("p3", 2, 0));

        ArgumentCaptor<Queue> saved = ArgumentCaptor.forClass(Queue.class);
        verify(snapshotStore).put(eq(ROOM), saved.capture());
        List<Play> q = saved.getValue().getQueue();
        assertThat(q).extracting(Play::getPlayId).containsExactly("p3", "p1", "p2");
        verify(broadcaster).broadcast(eq(ROOM), any(QueueViewPayload.class));
    }

    @Test
    void songReorder_oldIndexMismatch_triggersReseed() {
        Queue stale = queue(
                play("cur", 100, 30000, 0),
                new ArrayList<>(List.of(play("p1", 101, 60000, 0), play("p2", 102, 60000, 0))));
        Queue fresh = queue(play("cur", 100, 30000, 0), new ArrayList<>());
        when(snapshotStore.get(ROOM)).thenReturn(stale);
        when(queueClient.getQueue(ROOM)).thenReturn(fresh);
        when(managerRepository.tryAcquireEventLock(eq(MANAGER_NAME), anyLong())).thenReturn(null);

        // claims p2 was at oldIndex=0 — actually p1 is there
        handler.notify(ROOM, reorderEvent("p2", 0, 1));

        verify(queueClient).getQueue(ROOM);
        verify(snapshotStore).put(eq(ROOM), eq(fresh));
    }

    // ---------- helpers ----------

    private static Map<String, Object> updateEvent(String playId, int position) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event", "song_update");
        m.put("play_id", playId);
        m.put("position", position);
        m.put("duration", 30000);
        m.put("paused", false);
        return m;
    }

    private static Map<String, Object> reorderEvent(String playId, int oldIndex, int newIndex) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event", "song_reorder");
        m.put("play_id", playId);
        m.put("old_index", oldIndex);
        m.put("index", newIndex);
        return m;
    }

    private static Play play(String playId, int id, Integer duration, Integer position) {
        Play p = new Play();
        p.setPlayId(playId);
        p.setId(id);
        p.setDuration(duration);
        p.setPosition(position);
        return p;
    }

    private static Queue queue(Play current, List<Play> queue) {
        Queue q = new Queue();
        q.setRoomCode(ROOM);
        q.setCurrentSong(current);
        q.setQueue(new ArrayList<>(queue));
        return q;
    }

    private static UserQueue userQueueWith(String playId) {
        UserQueue uq = new UserQueue(ROOM);
        uq.setOrganization(null);
        Play p = new Play();
        p.setPlayId(playId);
        uq.getQueued().add(p);
        return uq;
    }
}
