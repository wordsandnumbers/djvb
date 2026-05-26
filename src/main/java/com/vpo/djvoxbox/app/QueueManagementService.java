package com.vpo.djvoxbox.app;

import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;
import com.vpo.djvoxbox.faye.QueueSnapshotStore;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.queue.PlayRequest;
import com.vpo.vbclient.queue.QueueClient;

/**
 * Per-room queue logic shared by two paths:
 * <ul>
 *   <li>The long-cadence reconciliation sweep in
 *       {@link UpdateService#update()} — calls {@link #manageQueues()} which
 *       walks every UserQueue, refreshes the upstream snapshot, recomputes
 *       estimated play times, and applies the per-mode play-next automation.</li>
 *   <li>The Faye event path in {@code com.vpo.djvoxbox.faye.QueueEventHandler}
 *       — calls {@link #handleCurrentSongChanged(String)} when a
 *       {@code song_update} indicates the currently-playing song rolled over,
 *       so the same per-mode automation runs without waiting for the next
 *       reconciliation tick.</li>
 * </ul>
 *
 * <p>The upstream {@link Queue} is now read from {@link QueueSnapshotStore} as
 * the primary source; an HTTP fetch via {@link QueueClient} is only performed
 * on cache miss (or when the cached room code does not match), and the result
 * is written back to the store so subsequent calls and event handlers see the
 * fresh view.
 */
@Service
public class QueueManagementService {

    @Autowired
    UserQueueRepository userQueueRepository;

    @Autowired
    QueueClient queueClient;

    @Autowired
    QueueSnapshotStore queueSnapshotStore;

    @Value("${vb.organization}")
    private String vbOrganization;

    /**
     * @return all UserQueues sorted by room code (so the upstream fetch can be
     *         deduplicated across consecutive rows for the same room).
     */
    private List<UserQueue> getAllQueues() {
        return userQueueRepository.findAll(Sort.by(Sort.Direction.ASC, "roomCode"));
    }

    /**
     * Resolve the upstream queue for {@code roomCode}, preferring the Redis
     * snapshot. On miss (or roomCode change between consecutive UserQueues),
     * fall back to an HTTP fetch and reseed the snapshot.
     *
     * <p>Returns {@code null} when the upstream returns 401 (the room code is
     * no longer valid for this organization); the caller demotes the
     * UserQueue accordingly.
     */
    private ConvenientQueue getQueue(final String roomCode, final ConvenientQueue queue) {
        if (queue != null && queue.getQueue() != null && queue.getQueue().getRoomCode() != null
                && queue.getQueue().getRoomCode().equals(roomCode)) {
            return queue;
        }
        // Try snapshot store first.
        Queue cached = queueSnapshotStore.get(roomCode);
        if (cached != null) {
            return new ConvenientQueue(cached);
        }
        ConvenientQueue q = null;
        try {
            Queue fresh = queueClient.getQueue(roomCode);
            if (fresh != null) {
                queueSnapshotStore.put(roomCode, fresh);
            }
            q = new ConvenientQueue(fresh);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() != HttpStatus.SC_UNAUTHORIZED) {
                throw e;
            }
        }
        return q;
    }

    /**
     * Full reconciliation sweep: walk every UserQueue, refresh its upstream
     * snapshot, recompute {@code estimatedPlayTime}s, and apply per-mode
     * play-next logic. Invoked from the 5-minute scheduler in
     * {@link UpdateService}.
     */
    public void manageQueues() {
        List<UserQueue> uqs = getAllQueues();
        ConvenientQueue q = null;
        for (UserQueue uq : uqs) {
            if (!sameOrg(uq)) {
                continue;
            }
            q = getQueue(uq.getRoomCode(), q);
            if (q == null || q.getQueue() == null) {
                downgradeQueueStatus(uq);
                continue;
            } else if (!uq.isActive()) {
                uq.setActive(true);
            }
            int playCount = applyEstimatedPlayTimes(uq, q);
            applyMode(uq, q, playCount);
            userQueueRepository.save(uq);
        }
    }

    /**
     * Faye-triggered narrow path. Called when a {@code song_update} event
     * indicates the currently-playing song has changed for {@code roomCode}.
     * Re-evaluates only the UserQueues belonging to that room.
     *
     * <p>Caller is expected to have already mutated the snapshot in
     * {@link QueueSnapshotStore} so this method sees the post-rollover queue.
     *
     * <p>Threading: must only be called from the persist+automate phase of
     * {@link com.vpo.djvoxbox.faye.QueueEventHandler}, i.e. while holding the
     * Manager event-lock — it issues {@code queueClient.addSong} calls and
     * persists UserQueues, both of which must be single-writer across
     * instances.
     */
    public void handleCurrentSongChanged(String roomCode) {
        Queue cached = queueSnapshotStore.get(roomCode);
        if (cached == null) {
            // Snapshot was evicted concurrently; the next reconciliation will catch up.
            return;
        }
        ConvenientQueue q = new ConvenientQueue(cached);
        List<UserQueue> uqs = userQueueRepository.findByRoomCode(roomCode);
        for (UserQueue uq : uqs) {
            if (!sameOrg(uq)) {
                continue;
            }
            int playCount = applyEstimatedPlayTimes(uq, q);
            applyMode(uq, q, playCount);
            userQueueRepository.save(uq);
        }
    }

    /**
     * Recompute {@code estimatedPlayTime} for each play this UserQueue has
     * queued upstream, drop plays the upstream no longer knows about, and
     * return the count of plays that remain (the "playCount" used by the
     * per-mode logic).
     */
    private int applyEstimatedPlayTimes(UserQueue uq, ConvenientQueue q) {
        int playCount = 0;
        for (int i = 0; i < uq.getQueued().size(); i++) {
            Play up = uq.getQueued().get(i);
            if (q.getPlayData().containsKey((up.getPlayId()))) {
                up.setEstimatedPlayTime(q.getPlayData().get(up.getPlayId()));
                playCount++;
            } else if (up.getPlayId() != null) {
                uq.getQueued().remove(i--);
            }
        }
        return playCount;
    }

    /**
     * Apply the user's per-queue mode policy. Extracted so both the
     * reconciliation sweep and the Faye event path use the same rules.
     */
    private void applyMode(UserQueue uq, ConvenientQueue q, int playCount) {
        String mode = (uq.getMode() != null) ? uq.getMode() : "default";
        switch (mode) {
            // put in a song as soon as there are at least X songs after you in the queue
            case "metered":
                if (playCount == 0 && uq.getQueue().size() != 0) {
                    playNext(uq);
                } else if (playCount != 0 && uq.getQueue().size() != 0) {
                    Play lastPlay = uq.getQueued().get(uq.getQueued().size() - 1);
                    if (lastPlay != null) {
                        Integer location = q.getQueue().getQueue().indexOf(lastPlay);
                        if (location != null && uq.getQueueInterval() != null
                                && (q.getQueue().getQueue().size() - (location + 1) >= uq.getQueueInterval())) {
                            playNext(uq);
                        }
                    }
                }
                break;
            case "manual":
                // we don't do anything in manual mode
                break;
            default:
                if (playCount == 0 && uq.getQueue().size() != 0) {
                    playNext(uq);
                }
                break;
        }
    }

    public void playNext(UserQueue uq) {
        Play nextPlay = uq.getQueue().get(0);
        Play newPlay = null;
        try {
            newPlay = queueClient.addSong(new PlayRequest(uq.getRoomCode(), nextPlay.getId()), uq.getSession());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() != HttpStatus.SC_NOT_FOUND) {
                throw e;
            }
        }
        if (newPlay != null) {
            nextPlay.setPlayId(newPlay.getPlayId());
            nextPlay.setPosition(newPlay.getPosition());
            nextPlay.setIndex(newPlay.getIndex());
            uq.getQueued().add(nextPlay);
        }
        uq.getQueue().remove(0);
    }

    private boolean sameOrg(UserQueue uq) {
        if (uq.getOrganization() == null && vbOrganization == null)
            return true;
        if (uq.getOrganization() != null && uq.getOrganization().equals(vbOrganization))
            return true;
        return false;
    }

    private void downgradeQueueStatus(UserQueue uq) {
        if (uq.isActive()) {
            uq.setActive(false);
            userQueueRepository.save(uq);
        } else {
            userQueueRepository.delete(uq);
        }
    }
}
