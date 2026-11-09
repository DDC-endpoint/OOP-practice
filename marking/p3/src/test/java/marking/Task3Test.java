package marking;

import edu.practice.scheduler.dto.CommitRequest;
import edu.practice.scheduler.dto.PlanRequest;
import edu.practice.scheduler.dto.Rejection;
import edu.practice.scheduler.dto.Reservation;
import edu.practice.scheduler.dto.SchedulePlan;
import edu.practice.scheduler.service.SchedulerService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static marking.Stubs.w;
import static marking.Stubs.wd;
import static org.junit.jupiter.api.Assertions.*;

/**
 * P3 Task 3 – Deterministic preview scheduling [20 marks].
 * Uses stub workloads with FIXED urgencies, so only the scheduler is being marked here.
 * Stubs.w(id, cpu, memory, duration, release, urgency); Stubs.wd(..., latestStart) adds a deadline.
 * Every reservation is a half-open interval [startTick, endTickExclusive).
 */
class Task3Test {

    static SchedulerService service(Stubs.W... workloads) {
        return new SchedulerService(new Stubs.StubRepo(workloads));
    }

    static PlanRequest req(int nowTick, int horizon, int cpu, int mem) {
        return new PlanRequest(nowTick, horizon, cpu, mem);
    }

    static Reservation res(long id, int start, int end, int cpu, int mem) {
        return new Reservation(id, start, end, cpu, mem);
    }

    // ---- 5 marks: validate PlanRequest ranges ----

    @Test
    void t3_previewRejectsOutOfRangeRequests_m3() {
        SchedulerService s = service(w(1, 1, 1, 1, 0, 10));
        for (PlanRequest bad : List.of(
                req(-1, 6, 6, 10000), req(1_000_001, 6, 6, 10000),
                req(0, 0, 6, 10000), req(0, 25, 6, 10000),
                req(0, 6, 0, 10000), req(0, 6, 65, 10000),
                req(0, 6, 6, 0), req(0, 6, 6, 1_000_001))) {
            assertThrows(IllegalArgumentException.class, () -> s.preview(bad), "must reject " + bad);
        }
    }

    @Test
    void t3_previewAcceptsBoundaryValues_m2() {
        SchedulerService s = service(w(1, 1, 1, 1, 0, 10));
        assertEquals(List.of(res(1, 1_000_000, 1_000_001, 1, 1)),
                s.preview(req(1_000_000, 24, 64, 1_000_000)).selected(), "upper bounds are allowed");
        assertEquals(List.of(res(1, 0, 1, 1, 1)), s.preview(req(0, 1, 1, 1)).selected(), "lower bounds are allowed");
    }

    // ---- 5 marks: ranked order; earliest = max(nowTick, release); latest = min(nowTick + horizon - duration, latestStartTick()) ----

    @Test
    void t3_previewProcessesWorkloadsInRankedOrder_m1() {
        // Both need all 4 cores for 2 ticks: the more urgent one (id 2) gets tick 0.
        SchedulePlan p = service(w(1, 4, 10, 2, 0, 10), w(2, 4, 10, 2, 0, 50)).preview(req(0, 4, 4, 100));
        assertEquals(List.of(res(2, 0, 2, 4, 10), res(1, 2, 4, 4, 10)), p.selected(), "selected are listed in ranked order");
        // Listed in ranked order even when the first-ranked starts later.
        SchedulePlan q = service(w(5, 1, 1, 1, 3, 99), w(6, 1, 1, 1, 0, 1)).preview(req(0, 6, 4, 100));
        assertEquals(List.of(res(5, 3, 4, 1, 1), res(6, 0, 1, 1, 1)), q.selected());
    }

    @Test
    void t3_previewEarliestStartIsMaxOfNowAndRelease_m2() {
        SchedulerService s = service(w(1, 1, 1, 2, 5, 10));
        assertEquals(List.of(res(1, 5, 7, 1, 1)), s.preview(req(0, 10, 4, 100)).selected(), "cannot start before release 5");
        assertEquals(List.of(res(1, 7, 9, 1, 1)), s.preview(req(7, 10, 4, 100)).selected(), "cannot start before nowTick 7");
        assertEquals(List.of(res(2, 3, 4, 2, 2)), service(w(2, 2, 2, 1, 0, 10)).preview(req(3, 5, 4, 100)).selected());
    }

    @Test
    void t3_previewLatestStartIsWindowOrDeadline_m2() {
        // Blocker 10 holds all 4 cores on [0,2). Job 11 (3 ticks) in a 4-tick window may start at 0 or 1 only.
        SchedulePlan narrow = service(w(10, 4, 1, 2, 0, 900), w(11, 1, 1, 3, 0, 100)).preview(req(0, 4, 4, 100));
        assertEquals(List.of(res(10, 0, 2, 4, 1)), narrow.selected(), "job 11 would end after the horizon");
        assertEquals(List.of(11L), narrow.rejected().stream().map(Rejection::id).toList());
        SchedulePlan wide = service(w(10, 4, 1, 2, 0, 900), w(11, 1, 1, 3, 0, 100)).preview(req(0, 5, 4, 100));
        assertEquals(res(11, 2, 5, 1, 1), wide.selected().get(1), "a 5-tick window allows start 2");
        // Deadline: latest legal start 1 means job 12 cannot start at 2 even though the window allows it.
        SchedulePlan dl = service(w(10, 4, 1, 2, 0, 900), wd(12, 1, 1, 2, 0, 100, 1)).preview(req(0, 10, 4, 100));
        assertEquals(List.of(10L), dl.selected().stream().map(Reservation::id).toList());
        assertEquals(List.of(12L), dl.rejected().stream().map(Rejection::id).toList());
        SchedulePlan dl2 = service(w(10, 4, 1, 2, 0, 900), wd(12, 1, 1, 2, 0, 100, 2)).preview(req(0, 10, 4, 100));
        assertEquals(res(12, 2, 4, 1, 1), dl2.selected().get(1), "latest legal start 2 is allowed");
    }

    // ---- 5 marks: earliest feasible start; EVERY occupied tick; confirmed + this preview within CPU and memory ----

    @Test
    void t3_previewChecksCpuOnEveryTick_m2() {
        // Job 1 (3 cores) holds [2,4). Job 2 (2 cores, 3 ticks) fits at tick 0 alone but would overlap at tick 2.
        SchedulePlan p = service(w(1, 3, 1, 2, 2, 900), w(2, 2, 1, 3, 0, 100)).preview(req(0, 10, 4, 100));
        assertEquals(List.of(res(1, 2, 4, 3, 1), res(2, 4, 7, 2, 1)), p.selected());
    }

    @Test
    void t3_previewChecksMemoryOnEveryTick_m1() {
        // Plenty of CPU; memory 600 + 500 > 1000 at the overlapping ticks.
        SchedulePlan p = service(w(1, 1, 600, 2, 2, 900), w(2, 1, 500, 3, 0, 100)).preview(req(0, 10, 64, 1000));
        assertEquals(List.of(res(1, 2, 4, 1, 600), res(2, 4, 7, 1, 500)), p.selected());
    }

    @Test
    void t3_previewHalfOpenIntervalsAllowBackToBack_m1() {
        SchedulePlan p = service(w(1, 4, 10, 2, 0, 900), w(2, 4, 10, 2, 0, 100)).preview(req(0, 6, 4, 100));
        assertEquals(List.of(res(1, 0, 2, 4, 10), res(2, 2, 4, 4, 10)), p.selected(), "[0,2) and [2,4) do not overlap");
    }

    @Test
    void t3_previewCountsConfirmedReservations_m1() {
        Stubs.StubRepo repo = new Stubs.StubRepo(w(1, 4, 10, 3, 0, 900));
        SchedulerService s = new SchedulerService(repo);
        SchedulePlan committed = s.commit(new CommitRequest("confirm-1", 0, 6, 4, 100));
        assertEquals(List.of(res(1, 0, 3, 4, 10)), committed.selected());
        repo.set(w(7, 1, 1, 1, 0, 50));   // a different workload; job 1's reservation is now confirmed
        assertEquals(List.of(res(7, 3, 4, 1, 1)), s.preview(req(0, 6, 4, 100)).selected(),
                "confirmed [0,3) uses all 4 cores, so job 7 starts at 3");
    }

    // ---- 5 marks: SchedulePlan with selected + rejected in ranked order, reasons; preview does not mutate ----

    @Test
    void t3_rejectsExceedsCapacity_m1() {
        SchedulePlan p = service(w(1, 8, 1, 1, 0, 50), w(2, 1, 20000, 1, 0, 40), w(3, 1, 1, 1, 0, 30))
                .preview(req(0, 6, 6, 10000));
        assertEquals(List.of(new Rejection(1, "EXCEEDS_CAPACITY"), new Rejection(2, "EXCEEDS_CAPACITY")), p.rejected(),
                "8 cores > 6 and 20000 MB > 10000");
        assertEquals(List.of(3L), p.selected().stream().map(Reservation::id).toList());
    }

    @Test
    void t3_rejectsReleaseOutsideWindow_m1() {
        SchedulePlan p = service(w(3, 1, 1, 1, 10, 50), w(4, 1, 1, 1, 6, 40), w(5, 1, 1, 1, 5, 30))
                .preview(req(0, 6, 6, 10000));
        assertEquals(List.of(new Rejection(3, "RELEASE_OUTSIDE_WINDOW"), new Rejection(4, "RELEASE_OUTSIDE_WINDOW")), p.rejected(),
                "window is [0,6): releases 10 and 6 are outside it");
        assertEquals(List.of(res(5, 5, 6, 1, 1)), p.selected(), "release 5 still fits [5,6)");
    }

    @Test
    void t3_rejectsDeadline_m1() {
        SchedulePlan p = service(wd(4, 1, 1, 2, 0, 50, 1), wd(5, 1, 1, 2, 0, 40, 3)).preview(req(3, 6, 6, 10000));
        assertEquals(List.of(new Rejection(4, "DEADLINE")), p.rejected(), "latest start 1 is before nowTick 3");
        assertEquals(List.of(res(5, 3, 5, 1, 1)), p.selected(), "latest start 3 = nowTick is still legal");
    }

    @Test
    void t3_rejectsNoSlotInRankedOrder_m1() {
        // Blocker 10 holds all 4 cores for the whole window [0,6); 5 and 6 never fit. Rejected in ranked order: 6 then 5.
        SchedulePlan p = service(w(5, 1, 1, 1, 0, 1), w(10, 4, 1, 6, 0, 900), w(6, 2, 1, 1, 0, 2)).preview(req(0, 6, 4, 100));
        assertEquals(List.of(new Rejection(6, "NO_SLOT"), new Rejection(5, "NO_SLOT")), p.rejected());
        assertEquals(List.of(res(10, 0, 6, 4, 1)), p.selected());
        assertEquals(0, p.nowTick());
        assertEquals(6, p.horizon());
    }

    @Test
    void t3_previewDoesNotMutateConfirmedState_m1() {
        SchedulerService s = service(w(1, 4, 10, 2, 0, 900), w(2, 4, 10, 2, 0, 100));
        SchedulePlan first = s.preview(req(0, 6, 4, 100));
        assertEquals(2, first.selected().size(), "both jobs fit in an empty 6-tick window");
        assertEquals(first, s.preview(req(0, 6, 4, 100)), "a second preview must see the same free cluster");
        assertEquals(first, s.preview(req(0, 6, 4, 100)));
    }
}
