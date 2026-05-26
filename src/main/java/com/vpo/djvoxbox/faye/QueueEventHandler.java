package com.vpo.djvoxbox.faye;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpo.djvoxbox.app.ConvenientQueue;
import com.vpo.djvoxbox.app.QueueManagementService;
import com.vpo.djvoxbox.domain.Manager;
import com.vpo.djvoxbox.domain.ManagerRepository;
import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.queue.QueueClient;

/**
 * Central dispatch for Faye queue events ({@code song_update} and
 * {@code song_reorder}). Implements the two-phase pattern described in the
 * design doc:
 *
 * <ol>
 *   <li><b>Notify</b> ({@link #notify(String, Object)}) — runs on every
 *       instance for every event, without acquiring any lock. Mutates the
 *       Redis snapshot, recomputes estimated play times, and hands the
 *       resulting {@link QueueViewPayload} to the {@link QueueEventBroadcaster}.
 *       Idempotent: parallel writes from N instances converge on the same
 *       state.</li>
 *   <li><b>Persist &amp; automate</b> ({@link #persistAndAutomate(String, boolean)})
 *       — runs only on the instance that wins
 *       {@link ManagerRepository#tryAcquireEventLock(String, long)}. Saves
 *       {@link UserQueue} rows and, on a currentSong rollover, invokes
 *       {@link QueueManagementService#handleCurrentSongChanged(String)} which
 *       may call upstream {@code addSong} for the per-mode play-next logic.</li>
 * </ol>
 *
 * <p>The split means a future SSE push-to-UI implementation of
 * {@link QueueEventBroadcaster} reaches its own connected browsers on every
 * instance, while only one instance is responsible for durable writes and
 * upstream side effects.
 */
@Service
public class QueueEventHandler {

    private static final Logger log = LoggerFactory.getLogger(QueueEventHandler.class);

    private final QueueSnapshotStore queueSnapshotStore;
    private final UserQueueRepository userQueueRepository;
    private final ManagerRepository managerRepository;
    private final QueueClient queueClient;
    private final QueueEventBroadcaster broadcaster;
    private final QueueManagementService queueManagementService;
    private final ObjectMapper objectMapper;

    @Value("${manager.name}")
    private String managerName;

    @Value("${manager.eventLockTtlMs:15000}")
    private long eventLockTtlMs;

    // Per-room serialization. Multiple Faye listener threads can deliver
    // events for different rooms concurrently; for a single room we still
    // want notify to be atomic with respect to itself so the snapshot
    // mutate-read-write sequence doesn't interleave.
    private final java.util.concurrent.ConcurrentHashMap<String, ReentrantLock> roomLocks =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    public QueueEventHandler(
            QueueSnapshotStore queueSnapshotStore,
            UserQueueRepository userQueueRepository,
            ManagerRepository managerRepository,
            QueueClient queueClient,
            QueueEventBroadcaster broadcaster,
            QueueManagementService queueManagementService,
            ObjectMapper objectMapper) {
        this.queueSnapshotStore = queueSnapshotStore;
        this.userQueueRepository = userQueueRepository;
        this.managerRepository = managerRepository;
        this.queueClient = queueClient;
        this.broadcaster = broadcaster;
        this.queueManagementService = queueManagementService;
        this.objectMapper = objectMapper;
    }

    /**
     * Entry point invoked by the Faye channel listener. The raw payload is
     * the {@code data} object from a Bayeux message; this method routes it to
     * {@link #handleSongUpdate} or {@link #handleSongReorder} based on the
     * {@code event} discriminator.
     *
     * <p>Swallows all exceptions and logs them — a broken event must not
     * tear down the BayeuxClient's listener thread.
     */
    public void notify(String roomCode, Object rawData) {
        try {
            if (rawData == null) {
                return;
            }
            Map<?, ?> asMap = (rawData instanceof Map) ? (Map<?, ?>) rawData : null;
            if (asMap == null) {
                log.debug("ignoring non-map Faye payload for room={} type={}", roomCode, rawData.getClass());
                return;
            }
            Object event = asMap.get("event");
            if ("song_update".equals(event)) {
                SongUpdateEvent e = objectMapper.convertValue(rawData, SongUpdateEvent.class);
                handleSongUpdate(roomCode, e);
            } else if ("song_reorder".equals(event)) {
                SongReorderEvent e = objectMapper.convertValue(rawData, SongReorderEvent.class);
                handleSongReorder(roomCode, e);
            } else {
                log.debug("ignoring unknown Faye event room={} event={}", roomCode, event);
            }
        } catch (Exception e) {
            log.warn("error handling Faye event room={} data={}", roomCode, rawData, e);
        }
    }

    /**
     * Apply a {@code song_update} event: either a position tick on the
     * currently-playing song or a rollover to a song previously in the queue.
     * Always runs the notify phase; then attempts the persist phase.
     */
    void handleSongUpdate(String roomCode, SongUpdateEvent event) {
        ReentrantLock lock = roomLock(roomCode);
        boolean rolledOver;
        Queue snapshot;
        lock.lock();
        try {
            snapshot = queueSnapshotStore.get(roomCode);
            if (snapshot == null) {
                snapshot = reseedFromUpstream(roomCode);
                if (snapshot == null) {
                    return;
                }
            }
            rolledOver = applySongUpdate(snapshot, event);
            if (rolledOver && (snapshot.getCurrentSong() == null
                    || !event.playId().equals(snapshot.getCurrentSong().getPlayId()))) {
                // play_id not found in currentSong or queue — snapshot is stale.
                snapshot = reseedFromUpstream(roomCode);
                if (snapshot == null) {
                    return;
                }
            } else {
                queueSnapshotStore.put(roomCode, snapshot);
            }
            broadcastView(roomCode, snapshot, rolledOver);
        } finally {
            lock.unlock();
        }
        persistAndAutomate(roomCode, rolledOver);
    }

    /**
     * Apply a {@code song_reorder} event to the cached snapshot. If
     * {@code oldIndex} disagrees with the cached state, the snapshot is
     * evicted and reseeded from upstream rather than mutated incorrectly.
     */
    void handleSongReorder(String roomCode, SongReorderEvent event) {
        ReentrantLock lock = roomLock(roomCode);
        lock.lock();
        try {
            Queue snapshot = queueSnapshotStore.get(roomCode);
            if (snapshot == null) {
                snapshot = reseedFromUpstream(roomCode);
                if (snapshot == null) {
                    return;
                }
            }
            boolean ok = applySongReorder(snapshot, event);
            if (!ok) {
                log.info("song_reorder oldIndex mismatch room={} oldIndex={} newIndex={} playId={} — reseeding",
                        roomCode, event.oldIndex(), event.index(), event.playId());
                snapshot = reseedFromUpstream(roomCode);
                if (snapshot == null) {
                    return;
                }
            } else {
                queueSnapshotStore.put(roomCode, snapshot);
            }
            broadcastView(roomCode, snapshot, false);
        } finally {
            lock.unlock();
        }
        persistAndAutomate(roomCode, false);
    }

    /**
     * Mutate {@code snapshot} in place for a {@code song_update}.
     *
     * @return {@code true} if the currently-playing song rolled over (i.e.
     *         the event's playId did not match the snapshot's currentSong).
     *         When {@code true} but currentSong was nulled out, the caller
     *         should treat the snapshot as stale and reseed.
     */
    boolean applySongUpdate(Queue snapshot, SongUpdateEvent event) {
        Play current = snapshot.getCurrentSong();
        if (current != null && event.playId() != null && event.playId().equals(current.getPlayId())) {
            // Position tick on the current song.
            if (event.position() != null) current.setPosition(event.position());
            if (event.duration() != null) current.setDuration(event.duration());
            if (event.paused() != null) current.setPaused(event.paused());
            return false;
        }
        // Rollover: find the play in the queue, pop it (and everything before it),
        // and set as new currentSong.
        List<Play> queue = snapshot.getQueue();
        if (queue == null) {
            snapshot.setCurrentSong(null);
            return true;
        }
        int found = -1;
        for (int i = 0; i < queue.size(); i++) {
            if (event.playId() != null && event.playId().equals(queue.get(i).getPlayId())) {
                found = i;
                break;
            }
        }
        if (found < 0) {
            // play_id not in queue — caller will reseed.
            snapshot.setCurrentSong(null);
            return true;
        }
        Play newCurrent = queue.get(found);
        if (event.position() != null) newCurrent.setPosition(event.position());
        if (event.duration() != null) newCurrent.setDuration(event.duration());
        if (event.paused() != null) newCurrent.setPaused(event.paused());
        snapshot.setCurrentSong(newCurrent);
        // Drop everything from index 0..found (inclusive) — newCurrent now owns those.
        for (int i = found; i >= 0; i--) {
            queue.remove(i);
        }
        return true;
    }

    /**
     * Mutate {@code snapshot} in place for a {@code song_reorder}.
     *
     * @return {@code false} when the cached state is inconsistent with the
     *         event (oldIndex / playId mismatch); the caller should reseed.
     */
    boolean applySongReorder(Queue snapshot, SongReorderEvent event) {
        List<Play> queue = snapshot.getQueue();
        if (queue == null || event.oldIndex() == null || event.index() == null) {
            return false;
        }
        int oldIdx = event.oldIndex();
        int newIdx = event.index();
        if (oldIdx < 0 || oldIdx >= queue.size() || newIdx < 0 || newIdx > queue.size()) {
            return false;
        }
        Play at = queue.get(oldIdx);
        if (event.playId() != null && !event.playId().equals(at.getPlayId())) {
            return false;
        }
        queue.remove(oldIdx);
        // After removal, indices >= oldIdx shifted down by one; clamp newIdx.
        int target = Math.min(newIdx, queue.size());
        queue.add(target, at);
        return true;
    }

    /**
     * Compute and emit the {@link QueueViewPayload}. Always called in the
     * notify phase, regardless of lock acquisition.
     */
    private void broadcastView(String roomCode, Queue snapshot, boolean rolledOver) {
        ConvenientQueue cq = new ConvenientQueue(snapshot);
        Play current = snapshot.getCurrentSong();
        QueueViewPayload payload = new QueueViewPayload(
                roomCode,
                current != null ? current.getPlayId() : null,
                current != null ? current.getPosition() : null,
                current != null ? current.isPaused() : null,
                rolledOver,
                cq.getPlayData());
        broadcaster.broadcast(roomCode, payload);
    }

    /**
     * Phase 2: acquire the Manager event-lock and, if won, persist updated
     * UserQueues plus run play-next automation on rollovers. Skipped silently
     * if the lock is held by another instance.
     */
    void persistAndAutomate(String roomCode, boolean rolledOver) {
        Manager lock = managerRepository.tryAcquireEventLock(managerName, eventLockTtlMs);
        if (lock == null) {
            log.trace("eventLock not acquired room={} — skipping write", roomCode);
            return;
        }
        try {
            Queue snapshot = queueSnapshotStore.get(roomCode);
            if (snapshot == null) {
                return;
            }
            ConvenientQueue cq = new ConvenientQueue(snapshot);
            List<UserQueue> affected = userQueueRepository.findByRoomCode(roomCode);
            List<UserQueue> toSave = new ArrayList<>(affected.size());
            for (UserQueue uq : affected) {
                if (recomputeEstimatedPlayTimes(uq, cq)) {
                    toSave.add(uq);
                }
            }
            for (UserQueue uq : toSave) {
                userQueueRepository.save(uq);
            }
            if (rolledOver) {
                queueManagementService.handleCurrentSongChanged(roomCode);
            }
        } finally {
            managerRepository.releaseEventLock(lock);
        }
    }

    /**
     * Update {@code uq.queued[*].estimatedPlayTime} from the derived
     * {@link ConvenientQueue#getPlayData()}. Returns {@code true} when at
     * least one play time changed (so the caller can skip saving rows that
     * didn't move).
     */
    private boolean recomputeEstimatedPlayTimes(UserQueue uq, ConvenientQueue cq) {
        boolean changed = false;
        Map<String, Long> playData = cq.getPlayData();
        for (Play p : uq.getQueued()) {
            Long est = playData.get(p.getPlayId());
            if (est != null && !est.equals(p.getEstimatedPlayTime())) {
                p.setEstimatedPlayTime(est);
                changed = true;
            }
        }
        return changed;
    }

    private Queue reseedFromUpstream(String roomCode) {
        try {
            Queue fresh = queueClient.getQueue(roomCode);
            if (fresh != null) {
                queueSnapshotStore.put(roomCode, fresh);
            } else {
                queueSnapshotStore.evict(roomCode);
            }
            return fresh;
        } catch (HttpClientErrorException e) {
            log.warn("reseed failed room={} status={}", roomCode, e.getStatusCode());
            return null;
        }
    }

    private ReentrantLock roomLock(String roomCode) {
        return roomLocks.computeIfAbsent(roomCode, k -> new ReentrantLock());
    }
}
