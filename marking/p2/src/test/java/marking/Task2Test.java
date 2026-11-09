package marking;

import edu.practice.dispatch.dto.DispatchPlan;
import edu.practice.dispatch.dto.PlanLine;
import edu.practice.dispatch.model.DispatchMission;
import edu.practice.dispatch.repo.MissionRepository;
import edu.practice.dispatch.service.DispatchService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static marking.Stubs.m;
import static org.junit.jupiter.api.Assertions.*;

/**
 * P2 Task 2 – AirMission and dispatch planning [22 marks].
 * Paper: AirMission(103,8,1,3,true) energy = 3x8 + 8x1 + 2x3 = 38, priority 5.
 * Capacity 60, urgentOnly=false: sorted [103,101,102]; select 103, skip 101, select 102; used 56, remaining 4.
 * AirMission is a class YOU create, so it is reached through Reflect.
 */
class Task2Test {

    static final String AIR = "edu.practice.dispatch.model.AirMission";
    static final String SERVICE_SRC = "src/main/java/edu/practice/dispatch/service/DispatchService.java";

    static DispatchMission air(long id, int distanceKm, int loadKg, int windPenalty, boolean critical) {
        Object o = Reflect.construct(AIR, id, distanceKm, loadKg, windPenalty, critical);
        assertTrue(o instanceof DispatchMission, "AirMission must extend DispatchMission");
        return (DispatchMission) o;
    }

    static Object critical(Object air) {
        try {
            return Reflect.call(air, "isCritical");
        } catch (AssertionError notIs) {
            return Reflect.call(air, "getCritical");
        }
    }

    // ---- 7 marks: AirMission extends DispatchMission with windPenalty and critical; constructor and getters ----

    @Test
    void t2_airMissionExtendsDispatchMission_m2() {
        Class<?> c = Reflect.require(AIR);
        assertTrue(Reflect.extendsClass(c, DispatchMission.class), "AirMission must extend DispatchMission");
        assertFalse(Modifier.isAbstract(c.getModifiers()), "AirMission must be a concrete class");
    }

    @Test
    void t2_airMissionConstructorChains_m2() {
        DispatchMission a = air(103, 8, 1, 3, true);
        assertEquals(103L, a.getId());
        assertEquals(8, a.getDistanceKm());
        assertEquals(1, a.getLoadKg());
        DispatchMission b = air(250, 40, 9, 0, false);
        assertEquals(250L, b.getId());
        assertEquals(40, b.getDistanceKm());
        assertEquals(9, b.getLoadKg());
    }

    @Test
    void t2_airMissionWindPenaltyAndCriticalGetters_m3() {
        DispatchMission a = air(103, 8, 1, 3, true);
        assertEquals(3, Reflect.call(a, "getWindPenalty"));
        assertEquals(true, critical(a));
        DispatchMission b = air(104, 6, 1, 2, false);
        assertEquals(2, Reflect.call(b, "getWindPenalty"));
        assertEquals(false, critical(b));
    }

    // ---- 7 marks: energyUnits() = 3d + 8l + 2w; priority() = 5 if critical, otherwise 2 ----

    @Test
    void t2_airEnergyIllustrative103Is38_m2() {
        assertEquals(38, air(103, 8, 1, 3, true).energyUnits(), "paper: 3x8 + 8x1 + 2x3 = 38");
    }

    @Test
    void t2_airEnergyOtherInputs_m2() {
        assertEquals(30, air(104, 6, 1, 2, false).energyUnits(), "3x6 + 8x1 + 2x2");
        assertEquals(230, air(105, 50, 10, 0, true).energyUnits(), "3x50 + 8x10 + 2x0");
        assertEquals(14, air(106, 0, 0, 7, false).energyUnits(), "only the wind penalty");
        assertEquals(130000, air(107, 10000, 10000, 10000, false).energyUnits());
    }

    @Test
    void t2_airPriorityCritical5Otherwise2_m3() {
        assertEquals(5, air(103, 8, 1, 3, true).priority());
        assertEquals(2, air(104, 6, 1, 2, false).priority());
        assertEquals(5, air(108, 1, 0, 0, true).priority());
    }

    // ---- 8 marks: DispatchService.plan(capacity, urgentOnly) ----

    @Test
    void t2_planIllustrativeCapacity60_m2() {
        // Uses the repository seed: Ground 101 (39, p3), Ground 102 (18, p1), Air 103 (38, p5).
        DispatchPlan plan = new DispatchService(new MissionRepository()).plan(60, false);
        assertEquals(List.of(103L, 102L), ids(plan.selected()), "paper: select 103 then 102 (seed AirMission 103 in MissionRepository)");
        assertEquals(List.of(101L), plan.skipped(), "paper: skip 101 (39 does not fit in 22)");
        assertEquals(60, plan.capacity());
        assertEquals(56, plan.energyUsed());
        assertEquals(4, plan.energyRemaining());
    }

    @Test
    void t2_planOrdersByPriorityThenEnergyThenId_m2() {
        // Inserted out of order. Expected order: priority DESC, then energy ASC, then ID ASC.
        MissionRepository repo = new Stubs.StubRepo(m(9, 3, 20), m(1, 1, 1), m(4, 3, 20), m(7, 5, 40), m(12, 3, 15));
        DispatchPlan plan = new DispatchService(repo).plan(1000, false);
        assertEquals(List.of(7L, 12L, 4L, 9L, 1L), ids(plan.selected()));
        PlanLine first = plan.selected().get(0);
        assertEquals(5, first.priority(), "PlanLine carries the mission's priority");
        assertEquals(40, first.energyUnits(), "PlanLine carries the mission's energy");
        assertEquals(96, plan.energyUsed());
        assertEquals(904, plan.energyRemaining());
        assertEquals(List.of(), plan.skipped());
    }

    @Test
    void t2_planGreedyKeepsScanningAfterSkip_m2() {
        MissionRepository repo = new Stubs.StubRepo(m(1, 5, 50), m(2, 5, 30), m(3, 3, 25), m(4, 1, 10), m(5, 1, 10));
        // sorted [2(30), 1(50), 3(25), 4(10), 5(10)]; capacity 60: take 2 (30 left), skip 1, take 3 (5 left), skip 4, skip 5
        DispatchPlan plan = new DispatchService(repo).plan(60, false);
        assertEquals(List.of(2L, 3L), ids(plan.selected()));
        assertEquals(List.of(1L, 4L, 5L), plan.skipped(), "skipped IDs in the order they were skipped");
        assertEquals(55, plan.energyUsed());
        assertEquals(5, plan.energyRemaining());
        // An exact fit is selected: sorted [7(40),12(15),4(20),9(20),1(1)], capacity 75 -> 7, 12, 4 (remaining 0)
        MissionRepository repo2 = new Stubs.StubRepo(m(9, 3, 20), m(1, 1, 1), m(4, 3, 20), m(7, 5, 40), m(12, 3, 15));
        DispatchPlan exact = new DispatchService(repo2).plan(75, false);
        assertEquals(List.of(7L, 12L, 4L), ids(exact.selected()));
        assertEquals(List.of(9L, 1L), exact.skipped());
        assertEquals(0, exact.energyRemaining());
    }

    @Test
    void t2_planUrgentOnlyKeepsPriorityAtLeast3_m1() {
        MissionRepository repo = new Stubs.StubRepo(m(1, 5, 10), m(2, 3, 10), m(3, 2, 1), m(4, 1, 1));
        DispatchPlan urgent = new DispatchService(repo).plan(100, true);
        assertEquals(List.of(1L, 2L), ids(urgent.selected()), "priority 2 and 1 are excluded when urgentOnly=true");
        assertEquals(List.of(), urgent.skipped(), "excluded missions are not planned, so they are not 'skipped'");
        assertEquals(20, urgent.energyUsed());
        DispatchPlan all = new DispatchService(repo).plan(100, false);
        assertEquals(List.of(1L, 2L, 3L, 4L), ids(all.selected()));
    }

    @Test
    void t2_planWithoutTypeChecks_m1() {
        MissionRepository repo = new Stubs.StubRepo(m(5, 2, 9), m(6, 4, 9), air(7, 1, 1, 1, true));
        // Air 7: 3+8+2 = 13, priority 5 -> sorted [7, 6, 5]; capacity 25: take 7 (12 left), take 6 (3 left), skip 5
        DispatchPlan plan = new DispatchService(repo).plan(25, false);
        assertEquals(List.of(7L, 6L), ids(plan.selected()));
        assertEquals(List.of(5L), plan.skipped());
        SourceScan.assertNoTypeChecks(SERVICE_SRC, "GroundMission", "AirMission");
    }

    static List<Long> ids(List<PlanLine> lines) {
        return lines.stream().map(PlanLine::id).toList();
    }
}
