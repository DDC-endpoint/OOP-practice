package marking;

import edu.practice.scheduler.error.WorkloadNotFoundException;
import edu.practice.scheduler.model.Workload;
import edu.practice.scheduler.repo.WorkloadRepository;
import edu.practice.scheduler.service.SchedulerService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static marking.Stubs.w;
import static org.junit.jupiter.api.Assertions.*;

/**
 * P3 Task 2 – DeadlineWorkload and dynamic ranking [22 marks].
 * urgencyAt = 1000 - 5 x max(0, deadlineTick - nowTick - durationTicks); latestStartTick = deadlineTick - durationTicks.
 * Seed: DeadlineWorkload(2,4,6000,2,0,4) and DeadlineWorkload(4,3,6000,3,1,6).
 * Paper: at nowTick 0, deadline workload 2 ranks ahead of batch workload 1.
 * DeadlineWorkload is a class YOU create, so it is reached through Reflect.
 */
class Task2Test {

    static final String DEADLINE = "edu.practice.scheduler.model.DeadlineWorkload";

    static Workload deadline(long id, int cpu, int mem, int duration, int release, int deadlineTick) {
        Object o = Reflect.construct(DEADLINE, id, cpu, mem, duration, release, deadlineTick);
        assertTrue(o instanceof Workload, "DeadlineWorkload must extend Workload");
        return (Workload) o;
    }

    // ---- 7 marks: DeadlineWorkload with deadlineTick, constructor and getter ----

    @Test
    void t2_deadlineWorkloadExtendsWorkload_m2() {
        Class<?> c = Reflect.require(DEADLINE);
        assertTrue(Reflect.extendsClass(c, Workload.class), "DeadlineWorkload must extend Workload");
        assertFalse(Modifier.isAbstract(c.getModifiers()), "DeadlineWorkload must be a concrete class");
    }

    @Test
    void t2_deadlineWorkloadConstructorChains_m3() {
        Workload d = deadline(2, 4, 6000, 2, 0, 4);
        assertEquals(2L, d.getId());
        assertEquals(4, d.getCpuCores());
        assertEquals(6000, d.getMemoryMb());
        assertEquals(2, d.getDurationTicks());
        assertEquals(0, d.getReleaseTick());
        Workload e = deadline(4, 3, 6000, 3, 1, 6);
        assertEquals(4L, e.getId());
        assertEquals(3, e.getDurationTicks());
        assertEquals(1, e.getReleaseTick());
    }

    @Test
    void t2_deadlineWorkloadGetDeadlineTick_m2() {
        assertEquals(4, Reflect.call(deadline(2, 4, 6000, 2, 0, 4), "getDeadlineTick"));
        assertEquals(17, Reflect.call(deadline(9, 1, 1, 5, 2, 17), "getDeadlineTick"));
    }

    // ---- 7 marks: urgencyAt with slack, safe arithmetic; latestStartTick = deadlineTick - durationTicks ----

    @Test
    void t2_deadlineUrgencyFromSlack_m3() {
        Workload d2 = deadline(2, 4, 6000, 2, 0, 4);
        assertEquals(990, d2.urgencyAt(0), "slack 4-0-2 = 2 -> 1000 - 10");
        Workload d4 = deadline(4, 3, 6000, 3, 1, 6);
        assertEquals(985, d4.urgencyAt(0), "slack 3");
        assertEquals(990, d4.urgencyAt(1), "slack 2");
        assertEquals(1000, d4.urgencyAt(3), "slack 0");
        assertEquals(1000, d4.urgencyAt(5), "slack max(0, -2) = 0");
    }

    @Test
    void t2_deadlineUrgencyOtherInputsSafeArithmetic_m2() {
        Workload d = deadline(9, 1, 1, 5, 0, 100);
        assertEquals(525, d.urgencyAt(0), "slack 95 -> 1000 - 475");
        assertEquals(1000, d.urgencyAt(300));
        assertEquals(-4_998_995, deadline(10, 1, 1, 1, 0, 1_000_000).urgencyAt(0), "slack 999,999 -> 1000 - 4,999,995");
        // slack = Integer.MAX_VALUE - 1: 5 x slack does not fit an int. It must not wrap around to a wrong value.
        Workload huge = deadline(11, 1, 1, 1, 0, Integer.MAX_VALUE);
        int result;
        try {
            result = huge.urgencyAt(0);
        } catch (ArithmeticException | IllegalArgumentException expected) {
            return;
        }
        assertEquals(Integer.MIN_VALUE, result, "1000 - 5 x 2147483646 overflows int: use long arithmetic and reject (or clamp)");
    }

    @Test
    void t2_deadlineLatestStartTick_m2() {
        assertEquals(2, deadline(2, 4, 6000, 2, 0, 4).latestStartTick());
        assertEquals(3, deadline(4, 3, 6000, 3, 1, 6).latestStartTick());
        assertEquals(95, deadline(9, 1, 1, 5, 0, 100).latestStartTick());
    }

    // ---- 8 marks: ranked(nowTick) by urgency DESC then ID ASC; urgency(id, nowTick); missing ID -> not found ----

    @Test
    void t2_rankedIllustrativeNowTick0_m2() {
        // Seed at tick 0: D2 = 990, D4 = 985, B1 = 100, B3 = 100 (tie -> ID ASC)
        List<Long> ids = ids(new SchedulerService(new WorkloadRepository()).ranked(0));
        assertTrue(ids.indexOf(2L) >= 0 && ids.indexOf(2L) < ids.indexOf(1L), "paper: deadline workload 2 ranks ahead of batch 1 at tick 0");
        assertEquals(List.of(2L, 4L, 1L, 3L), ids, "seed DeadlineWorkload 2 and 4 in WorkloadRepository");
    }

    @Test
    void t2_rankedByUrgencyDescThenIdAsc_m2() {
        WorkloadRepository repo = new Stubs.StubRepo(w(9, 1, 1, 1, 0, 50), w(3, 1, 1, 1, 0, 70), w(12, 1, 1, 1, 0, 50),
                w(1, 1, 1, 1, 0, 10), w(5, 1, 1, 1, 0, 50));
        assertEquals(List.of(3L, 5L, 9L, 12L, 1L), ids(new SchedulerService(repo).ranked(0)));
    }

    @Test
    void t2_rankedUsesUrgencyAtTheGivenTick_m1() {
        // B1 ages (100 + 2 x 500 = 1100) and overtakes the deadline workloads (1000) at tick 500.
        assertEquals(List.of(1L, 3L, 2L, 4L), ids(new SchedulerService(new WorkloadRepository()).ranked(500)));
        assertEquals(List.of(2L, 4L, 1L, 3L), ids(new SchedulerService(new WorkloadRepository()).ranked(3)),
                "tick 3: D2 = D4 = 1000 (ID tie-break), B1 = 106, B3 = 104");
    }

    @Test
    void t2_urgencyByIdReturnsValue_m2() {
        SchedulerService service = new SchedulerService(new Stubs.StubRepo(w(5, 1, 1, 1, 0, 321), w(6, 1, 1, 1, 0, 45)));
        assertEquals(321, service.urgency(5, 0));
        assertEquals(45, service.urgency(6, 9));
    }

    @Test
    void t2_urgencyUnknownIdThrowsNotFound_m1() {
        SchedulerService service = new SchedulerService(new Stubs.StubRepo(w(5, 1, 1, 1, 0, 321)));
        assertThrows(WorkloadNotFoundException.class, () -> service.urgency(99, 0));
        assertThrows(WorkloadNotFoundException.class, () -> service.urgency(4, 3));
    }

    static List<Long> ids(List<Workload> workloads) {
        return workloads.stream().map(Workload::getId).toList();
    }
}
