package com.vpo.djvoxbox.faye;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.vpo.vbclient.model.Queue;

/**
 * Redis-backed cache of the most recent upstream {@link Queue} per karaoke
 * room.
 *
 * <p>The snapshot is the in-process authoritative mirror of upstream state:
 * incoming Faye events ({@code song_update}, {@code song_reorder}) mutate it,
 * and the existing estimated-play-time derivation in {@link com.vpo.djvoxbox.app.ConvenientQueue}
 * reads from it to compute UserQueue projections. Redis (rather than process
 * memory) is used so all ECS instances share the same view, and so a brief
 * instance restart does not require an upstream HTTP refetch.
 *
 * <p>Keys follow the convention {@code djvb:upstream-queue:{roomCode}}. A
 * 30-minute TTL bounds staleness in the rare case a room is never written to
 * again (e.g. abandoned UserQueues).
 *
 * <p>Serialization is delegated to the {@link RedisTemplate}'s configured
 * serializer (a {@code GenericJackson2JsonRedisSerializer} in
 * {@link com.vpo.djvoxbox.config.VoxBoxConfig}), which embeds the class name in
 * the payload so deserialization is type-safe across vbclient model versions.
 */
@Service
public class QueueSnapshotStore {

    static final String KEY_PREFIX = "djvb:upstream-queue:";
    static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public QueueSnapshotStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @return the cached snapshot for {@code roomCode}, or {@code null} if no
     *         snapshot is currently stored (caller should fall back to an
     *         HTTP fetch and {@link #put} the result).
     */
    public Queue get(String roomCode) {
        Object raw = redisTemplate.opsForValue().get(key(roomCode));
        return (raw instanceof Queue) ? (Queue) raw : null;
    }

    /**
     * Store (or replace) the snapshot for {@code roomCode}; resets TTL.
     */
    public void put(String roomCode, Queue queue) {
        if (queue == null) {
            evict(roomCode);
            return;
        }
        redisTemplate.opsForValue().set(key(roomCode), queue, TTL.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Remove the cached snapshot for {@code roomCode}. Used after a mutation
     * endpoint changes upstream state so the next event or reconciliation
     * pass reseeds from truth.
     */
    public void evict(String roomCode) {
        redisTemplate.delete(key(roomCode));
    }

    static String key(String roomCode) {
        return KEY_PREFIX + roomCode;
    }
}
