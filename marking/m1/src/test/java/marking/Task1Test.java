package marking;

import edu.practice.courier.model.Parcel;
import edu.practice.courier.model.StandardParcel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M1 Task 1 – Complete StandardParcel [18 marks].
 * Paper: 1,200 g at 180 p -> 3 blocks -> 540 p; 500 g at 250 p -> 250 p; 501 g at 250 p -> 500 p.
 * getRatePerBlockPence() is reached through Reflect so the tests compile before you add it.
 */
class Task1Test {

    static int price(int weightGrams, int ratePerBlockPence) {
        return new StandardParcel(9, "T-9", weightGrams, ratePerBlockPence).pricePence();
    }

    /** Validation may live in the constructor (as the paper asks) or in pricePence(): both are accepted. */
    static void assertRejected(int weightGrams, int ratePerBlockPence, String what) {
        Executable build = () -> new StandardParcel(9, "T-9", weightGrams, ratePerBlockPence).pricePence();
        assertThrows(IllegalArgumentException.class, build, what + " must throw IllegalArgumentException");
    }

    // ---- 4 marks: inheritance, private final rate field, constructor chaining, public getter ----

    @Test
    void t1_extendsParcelWithPrivateFinalRateField_m2() throws Exception {
        assertEquals(Parcel.class, StandardParcel.class.getSuperclass(), "StandardParcel must extend Parcel");
        Field rate;
        try {
            rate = StandardParcel.class.getDeclaredField("ratePerBlockPence");
        } catch (NoSuchFieldException e) {
            fail("Declare a field 'private final int ratePerBlockPence' in StandardParcel");
            return;
        }
        assertEquals(int.class, rate.getType(), "ratePerBlockPence is an int");
        assertTrue(Modifier.isFinal(rate.getModifiers()), "ratePerBlockPence must be final");
        assertTrue(Modifier.isPrivate(rate.getModifiers()), "ratePerBlockPence must be private");
    }

    @Test
    void t1_constructorChainsAndGetterReturnsRate_m2() {
        StandardParcel s = new StandardParcel(1, "CP-1001", 1200, 180);
        assertEquals(1L, s.getId());
        assertEquals("CP-1001", s.getTrackingCode());
        assertEquals(1200, s.getWeightGrams());
        assertEquals(180, Reflect.call(s, "getRatePerBlockPence"));
        StandardParcel other = new StandardParcel(77, "ZZ-77", 64, 999);
        assertEquals(77L, other.getId());
        assertEquals("ZZ-77", other.getTrackingCode());
        assertEquals(64, other.getWeightGrams());
        assertEquals(999, Reflect.call(other, "getRatePerBlockPence"));
    }

    // ---- 6 marks: pricePence() = ratePerBlockPence x number of STARTED 500 g blocks ----

    @Test
    void t1_pricePaperExamples_m2() {
        assertEquals(540, price(1200, 180), "paper: 1,200 g = 3 blocks x 180 = 540");
        assertEquals(250, price(500, 250), "paper: exactly 500 g is one block");
        assertEquals(500, price(501, 250), "paper: 501 g starts a second block");
    }

    @Test
    void t1_priceRoundsUpEveryStartedBlock_m2() {
        assertEquals(100, price(1, 100), "1 g is one started block");
        assertEquals(100, price(499, 100));
        assertEquals(200, price(1000, 100), "1,000 g is exactly two blocks");
        assertEquals(300, price(1001, 100));
        assertEquals(1500, price(7499, 100), "7,499 g = 15 started blocks");
        assertEquals(222, price(2600, 37), "2,600 g = 6 blocks x 37");
    }

    @Test
    void t1_roundingUpIsSafeNearIntMax_m2() {
        // (weight + 499) / 500 overflows for weights near Integer.MAX_VALUE; the right answer is 4,294,968 blocks.
        assertEquals(4_294_968, price(Integer.MAX_VALUE, 1));
        assertEquals(2_143_189_032, price(Integer.MAX_VALUE, 499), "4,294,968 x 499 still fits in an int");
    }

    // ---- 2 marks: priority() ----

    @Test
    void t1_priorityIsAlwaysOne_m2() {
        assertEquals(1, new StandardParcel(1, "CP-1001", 1200, 180).priority());
        assertEquals(1, new StandardParcel(8, "Q-8", 30_000, 10_000).priority());
    }

    // ---- 6 marks: reject non-positive weight/rate and a price that overflows int (IllegalArgumentException) ----

    @Test
    void t1_rejectsNonPositiveWeight_m2() {
        assertRejected(0, 180, "weightGrams 0");
        assertRejected(-1, 180, "weightGrams -1");
        assertRejected(Integer.MIN_VALUE, 50, "weightGrams Integer.MIN_VALUE");
    }

    @Test
    void t1_rejectsNonPositiveRate_m2() {
        assertRejected(1200, 0, "ratePerBlockPence 0");
        assertRejected(1200, -50, "ratePerBlockPence -50");
    }

    @Test
    void t1_overflowingPriceThrowsIllegalArgument_m2() {
        assertRejected(Integer.MAX_VALUE, 500, "4,294,968 x 500 = 2,147,484,000 > Integer.MAX_VALUE");
        assertRejected(1_000_000_000, 2_000, "2,000,000 blocks x 2,000");
        assertRejected(Integer.MAX_VALUE, Integer.MAX_VALUE, "MAX weight x MAX rate");
        assertEquals(2_000_000_000, price(1_000_000_000, 1_000), "a large price that fits must still be returned");
    }
}
