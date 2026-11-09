package marking;

import edu.practice.scheduler.SchedulerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static marking.Api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * P3 Section B – six functional checks against the running app [20 marks].
 * Seed: B1(cpu2,4000MB,3 ticks,release 0), D2(4,6000,2,0,deadline 4), B3(2,2000,2,1), D4(3,6000,3,1,deadline 6).
 * Illustrative request {"nowTick":0,"horizon":6,"cpuCapacity":6,"memoryCapacity":10000}, worked by hand:
 *   ranked [2,4,1,3]; D2 [0,2); D4 [2,5) (tick 1 would need 7 cores); B1 [0,3); B3 [3,5) (ticks 1 and 2 are full).
 */
@SpringBootTest(classes = SchedulerApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SectionBTest {

    static final List<List<Long>> ILLUSTRATIVE_SELECTED = List.of(
            List.of(2L, 0L, 2L, 4L, 6000L),
            List.of(4L, 2L, 5L, 3L, 6000L),
            List.of(1L, 0L, 3L, 2L, 4000L),
            List.of(3L, 3L, 5L, 2L, 2000L));

    @Autowired
    TestRestTemplate rest;

    // 3 – BatchWorkload urgency ages correctly and deadlines calculate latest legal start.
    @Test
    void b1_batchAgesAndDeadlineLatestStart_m3() {
        assertEquals(100, urgency(rest, 1, 0));
        assertEquals(110, urgency(rest, 1, 5), "100 + 2 x 5");
        assertEquals(106, urgency(rest, 3, 4), "100 + 2 x (4 - 1)");
        assertEquals(990, urgency(rest, 2, 0), "1000 - 5 x (4 - 0 - 2)");
        // D2's latest legal start is 4 - 2 = 2: at tick 2 it starts exactly then, at tick 3 it misses its deadline.
        Map<String, Object> at2 = postOk(rest, "/api/workloads/preview", plan(2, 6, 6, 10000));
        assertTrue(selected(at2).contains(List.of(2L, 2L, 4L, 4L, 6000L)), "D2 starts at its latest legal start 2: " + selected(at2));
        Map<String, Object> at3 = postOk(rest, "/api/workloads/preview", plan(3, 6, 6, 10000));
        assertTrue(rejected(at3).contains("2:DEADLINE"), "at tick 3 D2 is past its latest start: " + rejected(at3));
    }

    // 3 – Ranked list uses dynamic urgency and stable ID tie-breaks.
    @Test
    void b2_rankedDynamicWithIdTieBreak_m3() {
        assertEquals(List.of(2L, 4L, 1L, 3L), rankedIds(rest, 0), "990, 985, 100, 100 (1 before 3)");
        assertEquals(List.of(2L, 4L, 1L, 3L), rankedIds(rest, 3), "1000, 1000 (2 before 4), 106, 104");
        assertEquals(List.of(1L, 3L, 2L, 4L), rankedIds(rest, 500), "1100, 1098, 1000, 1000: the batch jobs aged past the deadlines");
    }

    // 3 – Preview respects CPU, memory, release times, deadlines and half-open intervals.
    @Test
    void b3_previewRespectsResourcesAndTime_m3() {
        Map<String, Object> p = postOk(rest, "/api/workloads/preview", ILLUSTRATIVE);
        assertEquals(ILLUSTRATIVE_SELECTED, selected(p), "paper request: D2 (deadline) is placed ahead of B1");
        assertEquals(List.of(), rejected(p));
        // Memory 8000: B1 (4000 MB) never fits next to a 6000 MB deadline job; B3 fits at [1,3).
        Map<String, Object> m = postOk(rest, "/api/workloads/preview", plan(0, 6, 6, 8000));
        assertEquals(List.of(List.of(2L, 0L, 2L, 4L, 6000L), List.of(4L, 2L, 5L, 3L, 6000L), List.of(3L, 1L, 3L, 2L, 2000L)),
                selected(m));
        assertEquals(List.of("1:NO_SLOT"), rejected(m));
    }

    // 3 – Preview makes no persistent reservations, even after repeated calls.
    @Test
    void b4_previewMakesNoReservations_m3() {
        Map<String, Object> first = postOk(rest, "/api/workloads/preview", ILLUSTRATIVE);
        for (int i = 0; i < 3; i++) {
            Map<String, Object> again = postOk(rest, "/api/workloads/preview", ILLUSTRATIVE);
            assertEquals(selected(first), selected(again), "repeated previews must not see earlier previews");
        }
        postOk(rest, "/api/workloads/preview", plan(0, 4, 6, 10000));
        assertEquals(List.of(), reservations(rest), "preview must not create reservations");
        assertFalse(selected(first).isEmpty());
    }

    // 4 – Commit stores once; identical retry returns original plan and never double-books.
    @Test
    void b5_commitStoresSelectedOnce_m2() {
        Map<String, Object> c = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        assertFalse(selected(c).isEmpty(), "the paper's request selects workloads");
        assertEquals(sorted(selected(c)), sorted(rows(reservations(rest))), "exactly the committed selection is confirmed");
    }

    @Test
    void b5_identicalRetryReturnsOriginalNoDoubleBooking_m2() {
        Map<String, Object> c1 = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        Map<String, Object> c2 = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        assertEquals(selected(c1), selected(c2), "identical retry returns the original plan");
        assertEquals(rejected(c1), rejected(c2));
        assertEquals(selected(c1).size(), reservations(rest).size(), "paper: posting the identical commit twice leaves the count unchanged");
    }

    // 4 – Changed request on reused runKey returns 409; other invalid requests give 400.
    @Test
    void b6_changedRequestOnReusedRunKeyGives409_m2() {
        Map<String, Object> c = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        ResponseEntity<Map> r = rest.postForEntity("/api/workloads/commit", commit("trial-1", 0, 6, 6, 9000), Map.class);
        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        assertEquals("CONFLICT", r.getBody().get("code"));
        ResponseEntity<Map> r2 = rest.postForEntity("/api/workloads/commit", commit("trial-1", 1, 6, 6, 10000), Map.class);
        assertEquals(HttpStatus.CONFLICT, r2.getStatusCode());
        assertEquals(selected(c).size(), reservations(rest).size(), "a conflicting commit stores nothing");
    }

    @Test
    void b6_otherInvalidRequestsGive400_m2() {
        assertEquals(HttpStatus.BAD_REQUEST, rest.postForEntity("/api/workloads/preview", plan(0, 25, 6, 10000), Map.class).getStatusCode(), "horizon 25");
        assertEquals(HttpStatus.BAD_REQUEST, rest.postForEntity("/api/workloads/preview", plan(0, 6, 6, 0), Map.class).getStatusCode(), "memory 0");
        assertEquals(HttpStatus.BAD_REQUEST, rest.postForEntity("/api/workloads/commit", commit("t-x", -1, 6, 6, 10000), Map.class).getStatusCode(), "nowTick -1");
        assertEquals(HttpStatus.BAD_REQUEST, rest.postForEntity("/api/workloads/commit", commit("t-y", 0, 6, 0, 10000), Map.class).getStatusCode(), "cpu 0");
        assertEquals(HttpStatus.BAD_REQUEST, rest.getForEntity("/api/workloads/1/urgency?nowTick=-1", Map.class).getStatusCode(), "negative nowTick");
        assertEquals(List.of(), reservations(rest), "invalid commits store nothing");
    }

    static List<List<Long>> sorted(List<List<Long>> rows) {
        return rows.stream().sorted(Comparator.comparing(Object::toString)).toList();
    }
}
