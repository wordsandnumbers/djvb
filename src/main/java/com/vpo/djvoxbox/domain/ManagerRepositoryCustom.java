package com.vpo.djvoxbox.domain;

/**
 * Custom Mongo queries for the {@link Manager} singleton-coordination document.
 *
 * <p>The Manager record acts as a cross-instance lock: only the holder of the
 * lock performs side-effecting work (writing UserQueues, calling upstream add /
 * delete). Two lock flavors exist:
 * <ul>
 *   <li><b>{@code returnForWork} / {@code returnForUsurp}</b> — the long-cadence
 *       reconciliation lock used by
 *       {@link com.vpo.djvoxbox.app.UpdateService}. Held for the duration of a
 *       full sweep across all rooms (seconds to tens of seconds).</li>
 *   <li><b>{@code tryAcquireEventLock} / {@code releaseEventLock}</b> — a
 *       short-lived lock acquired per WebSocket event in
 *       {@link com.vpo.djvoxbox.faye.QueueEventHandler}. Expected to be held
 *       for a few milliseconds; a TTL guards against an instance dying
 *       mid-event.</li>
 * </ul>
 */
public interface ManagerRepositoryCustom {

    Manager returnForWork(String name);

    Manager returnForUsurp(String name);

    /**
     * Attempt to acquire the per-event write lock for the named Manager.
     *
     * <p>Implemented as a single Mongo {@code findAndModify} that only succeeds
     * when {@code workLock} is currently null OR older than {@code ttlMs}.
     * The returned Manager is the post-modification document with
     * {@code workLock} set to {@code now}; a {@code null} return means
     * another instance currently holds the lock and the caller should skip
     * its write phase (the notify phase still runs on every instance).
     *
     * @param name  Manager.name to lock on (typically {@code ${manager.name}}).
     * @param ttlMs maximum age in milliseconds for a stale lock to be
     *              considered abandoned and reclaimable.
     * @return the locked Manager, or {@code null} if not acquired.
     */
    Manager tryAcquireEventLock(String name, long ttlMs);

    /**
     * Release the per-event write lock by clearing {@code workLock}.
     * Safe to call even if the lock was never held (no-op when the document
     * is missing); does not validate ownership.
     */
    void releaseEventLock(Manager manager);
}
