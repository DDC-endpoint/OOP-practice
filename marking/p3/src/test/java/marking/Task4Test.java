package marking;

import edu.practice.scheduler.SchedulerApplication;
import edu.practice.scheduler.dto.PlanRequest;
import edu.practice.scheduler.dto.Reservation;
import edu.practice.scheduler.dto.SchedulePlan;
import edu.practice.scheduler.model.Workload;
import edu.practice.scheduler.service.SchedulerService;
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
 * P3 Task 4 – Commit semantics, conflict control and REST API [20 marks]. Runs the real app.
 * Expected plans come from the app's own SchedulerService.preview, so these marks are about
 * the endpoints and commit semantics rather than your Task 1-3 algorithm.
 */
@SpringBootTest(classes = SchedulerApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Task4Test {

    @Autowired
    TestRestTemplate rest;

    @Autowired
    SchedulerService service;

    // ---- 4 marks: GET /{id}/urgency, GET /ranked, POST /preview, GET /reservations ----

    @Test
    void t4_urgencyAndRankedEndpoints_m2() {
        assertEquals(service.urgency(2, 0), urgency(rest, 2, 0));
        assertEquals(service.urgency(1, 5), urgency(rest, 1, 5));
        for (int nowTick : new int[]{0, 500}) {
            assertEquals(service.ranked(nowTick).stream().map(Workload::getId).toList(), rankedIds(rest, nowTick));
        }
    }

    @Test
    void t4_previewAndReservationsEndpoints_m2() {
        assertEquals(List.of(), reservations(rest), "no reservations before any commit");
        Map<String, Object> p = postOk(rest, "/api/workloads/preview", ILLUSTRATIVE);
        SchedulePlan expected = service.preview(new PlanRequest(0, 6, 6, 10000));
        assertEquals(resRows(expected.selected()), selected(p));
        assertEquals(expected.rejected().stream().map(r -> r.id() + ":" + r.reason()).toList(), rejected(p));
        Map<String, Object> p2 = postOk(rest, "/api/workloads/preview", plan(0, 4, 6, 10000));
        assertEquals(resRows(service.preview(new PlanRequest(0, 4, 6, 10000)).selected()), selected(p2));
    }

    // ---- 4 marks: POST /commit; a new runKey persists the selected assignments ----

    @Test
    void t4_commitNewRunKeyReturnsPlan_m2() {
        SchedulePlan expected = service.preview(new PlanRequest(0, 6, 6, 10000));
        Map<String, Object> c = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        assertEquals(resRows(expected.selected()), selected(c), "commit returns the plan it stored");
        assertEquals(expected.rejected().stream().map(r -> r.id() + ":" + r.reason()).toList(), rejected(c));
    }

    @Test
    void t4_commitPersistsSelectedReservations_m2() {
        Map<String, Object> c = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        assertFalse(selected(c).isEmpty());
        assertEquals(sorted(selected(c)), sorted(rows(reservations(rest))), "confirmed reservations = the committed selection");
        assertEquals(sorted(selected(c)), sorted(resRows(service.reservations())));
    }

    // ---- 7 marks: identical retry returns the original plan, no duplicates; changed values -> 409 CONFLICT ----

    @Test
    void t4_identicalRetryReturnsOriginalPlan_m2() {
        Map<String, Object> first = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        // Another run fills the cluster, so recomputing trial-1 now would give a different plan.
        Map<String, Object> other = postOk(rest, "/api/workloads/commit", commit("trial-2", 0, 6, 6, 10000));
        assertNotEquals(selected(first), selected(other), "trial-2 must see trial-1's confirmed reservations");
        Map<String, Object> retry = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        assertEquals(selected(first), selected(retry), "retry must return the ORIGINAL stored plan, not a recomputed one");
        assertEquals(rejected(first), rejected(retry));
    }

    @Test
    void t4_identicalRetryDoesNotDuplicate_m2() {
        postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        List<List<Long>> before = rows(reservations(rest));
        postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        assertEquals(before.size(), reservations(rest).size(), "the reservation count must not change on retry");
        assertEquals(sorted(before), sorted(rows(reservations(rest))));
    }

    @Test
    void t4_changedRequestSameRunKeyGives409_m3() {
        postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        List<List<Long>> before = rows(reservations(rest));
        for (Map<String, Object> changed : List.of(commit("trial-1", 0, 5, 6, 10000), commit("trial-1", 0, 6, 8, 10000))) {
            ResponseEntity<Map> r = rest.postForEntity("/api/workloads/commit", changed, Map.class);
            assertEquals(HttpStatus.CONFLICT, r.getStatusCode(), "same runKey, changed values: " + changed);
            assertEquals("CONFLICT", r.getBody().get("code"));
            assertNotNull(r.getBody().get("message"));
        }
        assertEquals(sorted(before), sorted(rows(reservations(rest))), "a conflict must not change reservations");
    }

    // ---- 5 marks: invalid input -> 400, unknown workload -> 404, deterministic GET /reservations ----

    @Test
    void t4_invalidInputsGive400_m2() {
        for (String[] c : new String[][]{{"/api/workloads/preview", "h0"}, {"/api/workloads/commit", "cpu65"}}) {
            Object body = c[1].equals("h0") ? plan(0, 0, 6, 10000) : commit("bad-1", 0, 6, 65, 10000);
            ResponseEntity<Map> r = rest.postForEntity(c[0], body, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), c[0] + " " + body);
            assertNotNull(r.getBody().get("code"), "400 body must be an ApiError");
            assertNotNull(r.getBody().get("message"), "400 body must be an ApiError");
        }
        assertEquals(List.of(), reservations(rest), "an invalid commit stores nothing");
    }

    @Test
    void t4_unknownWorkloadGives404_m2() {
        for (long id : new long[]{99, 5}) {
            ResponseEntity<Map> r = rest.getForEntity("/api/workloads/" + id + "/urgency?nowTick=0", Map.class);
            assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
            assertNotNull(r.getBody().get("code"), "404 body must be an ApiError");
            assertNotNull(r.getBody().get("message"), "404 body must be an ApiError");
        }
    }

    @Test
    void t4_reservationsOrderIsDeterministic_m1() {
        Map<String, Object> c = postOk(rest, "/api/workloads/commit", commit("trial-1", 0, 6, 6, 10000));
        postOk(rest, "/api/workloads/commit", commit("trial-9", 0, 12, 6, 10000));
        List<List<Long>> a = rows(reservations(rest));
        assertEquals(a, rows(reservations(rest)), "repeat GETs must return the same order");
        boolean byStart = a.equals(a.stream().sorted(Comparator.<List<Long>>comparingLong(x -> x.get(1))
                .thenComparingLong(x -> x.get(0))).toList());
        boolean byId = a.equals(a.stream().sorted(Comparator.<List<Long>>comparingLong(x -> x.get(0))
                .thenComparingLong(x -> x.get(1))).toList());
        assertTrue(byStart || byId, "GET /reservations should use a defined order (startTick then id, or id then startTick): " + a);
        assertFalse(selected(c).isEmpty());
    }

    static List<List<Long>> resRows(List<Reservation> rs) {
        return rs.stream().map(r -> List.of(r.id(), (long) r.startTick(), (long) r.endTickExclusive(),
                (long) r.cpuCores(), (long) r.memoryMb())).toList();
    }

    static List<List<Long>> sorted(List<List<Long>> rows) {
        return rows.stream().sorted(Comparator.comparing(Object::toString)).toList();
    }
}
