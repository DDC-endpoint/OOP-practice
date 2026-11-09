package marking;

import edu.practice.scheduler.model.BatchWorkload;
import edu.practice.scheduler.model.Workload;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P3 Task 1 – Aging-aware BatchWorkload [18 marks].
 * urgencyAt(nowTick) = 100 + 2 x max(0, nowTick - releaseTick); latestStartTick() = Integer.MAX_VALUE.
 * Seed: BatchWorkload(1,2,4000,3,0) and BatchWorkload(3,2,2000,2,1).
 */
class Task1Test {

    // ---- 4 marks: BatchWorkload extends Workload; inherited fields set through the parent constructor ----

    @Test
    void t1_extendsWorkloadThroughParentConstructor_m2() {
        assertEquals(Workload.class, BatchWorkload.class.getSuperclass(), "BatchWorkload must extend Workload");
        BatchWorkload b = new BatchWorkload(1, 2, 4000, 3, 0);
        assertEquals(1L, b.getId());
        assertEquals(2, b.getCpuCores());
        assertEquals(4000, b.getMemoryMb());
    }

    @Test
    void t1_durationAndReleaseInitialised_m2() {
        BatchWorkload b = new BatchWorkload(3, 2, 2000, 2, 1);
        assertEquals(2, b.getDurationTicks());
        assertEquals(1, b.getReleaseTick());
        BatchWorkload c = new BatchWorkload(40, 7, 123, 9, 15);
        assertEquals(40L, c.getId());
        assertEquals(7, c.getCpuCores());
        assertEquals(123, c.getMemoryMb());
        assertEquals(9, c.getDurationTicks());
        assertEquals(15, c.getReleaseTick());
    }

    // ---- 8 marks: urgencyAt = 100 + 2 x max(0, nowTick - releaseTick); reject negative nowTick; no overflow ----

    @Test
    void t1_urgencyAgesFromRelease_m3() {
        BatchWorkload b1 = new BatchWorkload(1, 2, 4000, 3, 0);
        assertEquals(100, b1.urgencyAt(0));
        assertEquals(110, b1.urgencyAt(5));
        BatchWorkload b3 = new BatchWorkload(3, 2, 2000, 2, 1);
        assertEquals(100, b3.urgencyAt(0), "before release the age is max(0, -1) = 0");
        assertEquals(100, b3.urgencyAt(1));
        assertEquals(106, b3.urgencyAt(4));
    }

    @Test
    void t1_urgencyOtherInputs_m3() {
        BatchWorkload b = new BatchWorkload(9, 1, 1, 1, 10);
        assertEquals(130, b.urgencyAt(25), "100 + 2 x 15");
        assertEquals(100, b.urgencyAt(10));
        assertEquals(100, b.urgencyAt(3), "not yet released: no ageing");
        assertEquals(2_000_000_100, new BatchWorkload(10, 1, 1, 1, 0).urgencyAt(1_000_000_000));
    }

    @Test
    void t1_urgencyRejectsNegativeNowTick_m1() {
        BatchWorkload b = new BatchWorkload(1, 2, 4000, 3, 0);
        assertThrows(IllegalArgumentException.class, () -> b.urgencyAt(-1));
        assertThrows(IllegalArgumentException.class, () -> b.urgencyAt(-100));
    }

    @Test
    void t1_urgencyOverflowPrevented_m1() {
        BatchWorkload b = new BatchWorkload(11, 1, 1, 1, 0);
        for (int nowTick : new int[]{Integer.MAX_VALUE, 1_073_741_824}) {   // 100 + 2 x nowTick > Integer.MAX_VALUE
            int result;
            try {
                result = b.urgencyAt(nowTick);
            } catch (ArithmeticException | IllegalArgumentException expected) {
                continue;   // rejecting the overflow is correct
            }
            assertEquals(Integer.MAX_VALUE, result, "urgency at " + nowTick + " overflows int: use long + Math.toIntExact, never wrap");
        }
    }

    // ---- 6 marks: latestStartTick() = Integer.MAX_VALUE; polymorphic dispatch through Workload references ----

    @Test
    void t1_latestStartIsMaxValue_m3() {
        assertEquals(Integer.MAX_VALUE, new BatchWorkload(1, 2, 4000, 3, 0).latestStartTick());
        assertEquals(Integer.MAX_VALUE, new BatchWorkload(3, 2, 2000, 2, 1).latestStartTick());
        assertEquals(Integer.MAX_VALUE, new BatchWorkload(50, 1, 1, 24, 999).latestStartTick());
    }

    @Test
    void t1_polymorphicThroughWorkloadRefs_m3() throws Exception {
        assertEquals(BatchWorkload.class, BatchWorkload.class.getMethod("urgencyAt", int.class).getDeclaringClass());
        assertEquals(BatchWorkload.class, BatchWorkload.class.getMethod("latestStartTick").getDeclaringClass());
        List<Workload> all = List.of(new BatchWorkload(12, 2, 100, 4, 3), Stubs.w(13, 1, 1, 1, 0, 777));
        assertEquals(List.of(114, 777), all.stream().map(w -> w.urgencyAt(10)).toList(), "Batch 12 at tick 10: 100 + 2 x 7");
        assertEquals(Integer.MAX_VALUE, all.get(0).latestStartTick());
    }
}
