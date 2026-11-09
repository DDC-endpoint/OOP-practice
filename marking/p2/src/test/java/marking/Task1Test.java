package marking;

import edu.practice.dispatch.model.DispatchMission;
import edu.practice.dispatch.model.GroundMission;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P2 Task 1 – Implement GroundMission [18 marks].
 * Paper: GroundMission(101,12,3,true) energy = 2x12 + 5x3 = 39, priority 3.
 */
class Task1Test {

    // ---- 4 marks: extend DispatchMission, constructor chaining, isMedical() ----

    @Test
    void t1_extendsDispatchMissionAndChainsConstructor_m2() {
        assertEquals(DispatchMission.class, GroundMission.class.getSuperclass(), "GroundMission must extend DispatchMission");
        GroundMission g = new GroundMission(101, 12, 3, true);
        assertEquals(101L, g.getId());
        assertEquals(12, g.getDistanceKm());
        assertEquals(3, g.getLoadKg());
    }

    @Test
    void t1_isMedicalExposed_m2() {
        assertTrue(new GroundMission(101, 12, 3, true).isMedical());
        assertFalse(new GroundMission(102, 4, 2, false).isMedical());
    }

    // ---- 8 marks: energyUnits() = 2 x distanceKm + 5 x loadKg, long intermediate, safe int conversion ----

    @Test
    void t1_energyIllustrative101Is39_m3() {
        assertEquals(39, new GroundMission(101, 12, 3, true).energyUnits(), "paper: 2x12 + 5x3 = 39");
        assertEquals(18, new GroundMission(102, 4, 2, false).energyUnits(), "2x4 + 5x2 = 18");
    }

    @Test
    void t1_energyFormulaOtherInputs_m3() {
        assertEquals(235, new GroundMission(7, 100, 7, false).energyUnits(), "2x100 + 5x7");
        assertEquals(2, new GroundMission(8, 1, 0, true).energyUnits(), "2x1 + 5x0");
        assertEquals(50, new GroundMission(9, 0, 10, false).energyUnits(), "2x0 + 5x10");
        assertEquals(70000, new GroundMission(10, 10000, 10000, false).energyUnits());
    }

    @Test
    void t1_energyOverflowIsDetected_m2() {
        // 2 x 1,500,000,000 does not fit in an int: compute in long, then Math.toIntExact (throws).
        GroundMission huge = new GroundMission(11, 1_500_000_000, 0, false);
        RuntimeException e = assertThrows(RuntimeException.class, huge::energyUnits);
        assertTrue(e instanceof ArithmeticException || e instanceof IllegalArgumentException,
                "overflow must be rejected (ArithmeticException / IllegalArgumentException), got " + e);
        GroundMission load = new GroundMission(12, 0, 500_000_000, false);
        RuntimeException e2 = assertThrows(RuntimeException.class, load::energyUnits);
        assertTrue(e2 instanceof ArithmeticException || e2 instanceof IllegalArgumentException, "5 x 500,000,000 overflows int");
    }

    // ---- 6 marks: priority() = 3 medical / 1 otherwise; both methods override the abstract parent methods ----

    @Test
    void t1_priorityMedical3Otherwise1_m4() {
        assertEquals(3, new GroundMission(101, 12, 3, true).priority());
        assertEquals(1, new GroundMission(102, 4, 2, false).priority());
        assertEquals(3, new GroundMission(20, 1, 1, true).priority());
        assertEquals(1, new GroundMission(21, 999, 50, false).priority());
    }

    @Test
    void t1_methodsOverrideParentPolymorphically_m2() throws Exception {
        assertTrue(Modifier.isAbstract(DispatchMission.class.getMethod("energyUnits").getModifiers()));
        assertEquals(GroundMission.class, GroundMission.class.getMethod("energyUnits").getDeclaringClass());
        assertEquals(GroundMission.class, GroundMission.class.getMethod("priority").getDeclaringClass());
        DispatchMission m = new GroundMission(30, 5, 4, true);   // called through the parent type
        assertEquals(30, m.energyUnits());
        assertEquals(3, m.priority());
    }
}
