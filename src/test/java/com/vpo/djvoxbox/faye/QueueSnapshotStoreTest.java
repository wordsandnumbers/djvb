package com.vpo.djvoxbox.faye;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.vpo.vbclient.model.Queue;

/**
 * Verifies the Redis key layout and the put/get/evict round-trip.
 */
@ExtendWith(MockitoExtension.class)
class QueueSnapshotStoreTest {

    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOps;

    private QueueSnapshotStore store;

    @BeforeEach
    void setUp() {
        store = new QueueSnapshotStore(redisTemplate);
    }

    @Test
    void put_writesUnderPrefixedKeyWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        Queue q = new Queue();
        q.setRoomCode("MCHQ");

        store.put("MCHQ", q);

        verify(valueOps).set(
                eq("djvb:upstream-queue:MCHQ"),
                eq(q),
                eq(QueueSnapshotStore.TTL.toMillis()),
                eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void put_nullEvictsInsteadOfWriting() {
        store.put("MCHQ", null);
        verify(redisTemplate).delete("djvb:upstream-queue:MCHQ");
    }

    @Test
    void get_returnsCachedQueueWhenPresent() {
        Queue q = new Queue();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("djvb:upstream-queue:MCHQ")).thenReturn(q);

        assertThat(store.get("MCHQ")).isSameAs(q);
    }

    @Test
    void get_returnsNullOnMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);

        assertThat(store.get("UNKN")).isNull();
    }

    @Test
    void evict_deletesByKey() {
        store.evict("MCHQ");
        verify(redisTemplate).delete("djvb:upstream-queue:MCHQ");
    }
}
