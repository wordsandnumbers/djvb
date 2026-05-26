package com.vpo.djvoxbox.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;

/**
 * Pins the estimated-play-time derivation that was previously a private
 * inner class on QueueManagementService. Anchors the extraction and the
 * Faye event path's reliance on this math.
 */
class ConvenientQueueTest {

    @Test
    void nullQueue_emptyPlayData() {
        ConvenientQueue cq = new ConvenientQueue(null);
        assertThat(cq.getPlayData()).isEmpty();
    }

    @Test
    void onlyCurrentSong_mapsToNowApproximately() {
        Queue q = new Queue();
        q.setCurrentSong(play("cur", 1, 100, null, null));

        long before = System.currentTimeMillis();
        ConvenientQueue cq = new ConvenientQueue(q);
        long after = System.currentTimeMillis();

        Map<String, Long> data = cq.getPlayData();
        assertThat(data).containsOnlyKeys("cur");
        assertThat(data.get("cur")).isBetween(before, after);
    }

    @Test
    void currentSongWithDurationPlusQueue_accumulatesRemainingTime() {
        Queue q = new Queue();
        // currentSong has 10s remaining (duration 30000 - position 20000)
        q.setCurrentSong(play("cur", 1, 100, 30000, 20000));
        q.setQueue(new ArrayList<>(List.of(
                play("p1", 1, 101, 60000, 0),
                play("p2", 1, 102, 30000, 0))));

        long t0 = System.currentTimeMillis();
        ConvenientQueue cq = new ConvenientQueue(q);

        Map<String, Long> data = cq.getPlayData();
        assertThat(data).containsKeys("cur", "p1", "p2");
        // p1 starts after 10s remaining of cur
        assertThat(data.get("p1") - data.get("cur")).isEqualTo(10000L);
        // p2 starts after 10s remaining + 60s of p1
        assertThat(data.get("p2") - data.get("cur")).isEqualTo(70000L);
        // cur is anchored close to "now"
        assertThat(data.get("cur")).isGreaterThanOrEqualTo(t0).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    void nullDuration_fallsBackToFourMinuteHeuristic() {
        Queue q = new Queue();
        q.setCurrentSong(play("cur", 1, 100, 30000, 30000)); // 0ms remaining
        q.setQueue(new ArrayList<>(List.of(
                play("p1", 1, 101, null, null),  // null duration → 240000ms
                play("p2", 1, 102, 30000, 0))));

        ConvenientQueue cq = new ConvenientQueue(q);
        Map<String, Long> data = cq.getPlayData();
        // p2 starts 240000ms after p1 because p1 had null duration
        assertThat(data.get("p2") - data.get("p1")).isEqualTo(240000L);
    }

    @Test
    void nullCurrentSong_walkQueueFromNow() {
        Queue q = new Queue();
        q.setCurrentSong(null);
        q.setQueue(new ArrayList<>(List.of(play("p1", 1, 101, 30000, 0))));

        long t0 = System.currentTimeMillis();
        ConvenientQueue cq = new ConvenientQueue(q);
        long t1 = System.currentTimeMillis();

        // No currentSong recorded; queue starts at "now".
        assertThat(cq.getPlayData()).containsOnlyKeys("p1");
        assertThat(cq.getPlayData().get("p1")).isBetween(t0, t1);
    }

    private static Play play(String playId, int songId, int id, Integer duration, Integer position) {
        Play p = new Play();
        p.setPlayId(playId);
        p.setSongId(songId);
        p.setId(id);
        p.setDuration(duration);
        p.setPosition(position);
        return p;
    }
}
