package marking;

import edu.practice.parking.model.HourlyPass;
import edu.practice.parking.model.ParkingPass;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P1 Task 1 – Complete HourlyPass [18 marks].
 * Paper: 60 minutes -> 250 cents, 61 minutes -> 500 cents (rate 250).
 */
class Task1Test {

    // ---- 4 marks: inheritance, final hourly rate field, constructor chaining, public getter ----

    @Test
    void t1_extendsParkingPassWithFinalRateField_m2() {
        assertEquals(ParkingPass.class, HourlyPass.class.getSuperclass(), "HourlyPass must extend ParkingPass");
        boolean finalIntField = Arrays.stream(HourlyPass.class.getDeclaredFields())
                .anyMatch(f -> f.getType() == int.class && !Modifier.isStatic(f.getModifiers())
                        && Modifier.isFinal(f.getModifiers()) && Modifier.isPrivate(f.getModifiers()));
        assertTrue(finalIntField, "HourlyPass needs a private final int hourly rate field (rateCents)");
    }

    @Test
    void t1_constructorChainsToParentAndGetterWorks_m2() throws Exception {
        HourlyPass p = new HourlyPass(7, "CAR-707", 300);
        assertEquals(7L, p.getId(), "id must reach ParkingPass through super(...)");
        assertEquals("CAR-707", p.getRegistration(), "registration must reach ParkingPass through super(...)");
        assertEquals(300, p.getRateCents());
        assertTrue(Modifier.isPublic(HourlyPass.class.getMethod("getRateCents").getModifiers()));
    }

    // ---- 6 marks: ceiling division into billable hours (1-60 -> 1 hour, 61-120 -> 2) ----

    @Test
    void t1_oneToSixtyMinutesIsOneBillableHour_m3() {
        HourlyPass p = new HourlyPass(1, "CAR-101", 250);
        assertEquals(250, p.priceCents(60), "paper: 60 minutes -> 250 cents");
        assertEquals(250, p.priceCents(1), "1 minute is one whole billable hour");
        assertEquals(250, p.priceCents(30));
        assertEquals(400, new HourlyPass(9, "X-9", 400).priceCents(45));
    }

    @Test
    void t1_partialHourRoundsUp_m3() {
        HourlyPass p = new HourlyPass(1, "CAR-101", 250);
        assertEquals(500, p.priceCents(61), "paper: 61 minutes -> 500 cents");
        assertEquals(500, p.priceCents(119));
        assertEquals(750, p.priceCents(121), "121 minutes = 3 commenced hours");
        assertEquals(1200, new HourlyPass(9, "X-9", 300).priceCents(181), "181 minutes = 4 hours x 300");
    }

    // ---- 4 marks: rounded hours x rateCents, integer cents ----

    @Test
    void t1_priceIsHoursTimesRate_m2() {
        assertEquals(500, new HourlyPass(1, "A", 250).priceCents(120));
        assertEquals(900, new HourlyPass(2, "B", 300).priceCents(180));
        assertEquals(10, new HourlyPass(3, "C", 1).priceCents(600));
    }

    @Test
    void t1_priceUsesEachPassOwnRate_m2() {
        assertEquals(4995, new HourlyPass(4, "D", 999).priceCents(300), "5 hours x 999");
        assertEquals(4936, new HourlyPass(5, "E", 1234).priceCents(240), "4 hours x 1234");
    }

    // ---- 4 marks: reject zero/negative stayMinutes; prevent arithmetic overflow ----

    @Test
    void t1_rejectsZeroOrNegativeMinutes_m2() {
        HourlyPass p = new HourlyPass(1, "CAR-101", 250);
        assertThrows(IllegalArgumentException.class, () -> p.priceCents(0));
        assertThrows(IllegalArgumentException.class, () -> p.priceCents(-1));
        assertThrows(IllegalArgumentException.class, () -> p.priceCents(-60));
    }

    @Test
    void t1_overflowingPriceIsNotWrapped_m1() {
        HourlyPass p = new HourlyPass(5, "BIG-1", Integer.MAX_VALUE);
        int result;
        try {
            result = p.priceCents(61);   // 2 hours x Integer.MAX_VALUE does not fit in an int
        } catch (ArithmeticException | IllegalArgumentException expected) {
            return;                      // rejecting the overflow is correct
        }
        assertEquals(Integer.MAX_VALUE, result,
                "2 x Integer.MAX_VALUE overflows int. Use long maths + Math.toIntExact / Math.multiplyExact, "
                        + "do not return a wrapped value");
    }

    @Test
    void t1_hugeStayRoundsWithoutOverflow_m1() {
        // ceil(2147483647 / 60) = 35791395. (minutes + 59) / 60 in int arithmetic overflows here.
        assertEquals(35791395, new HourlyPass(6, "LONG-1", 1).priceCents(Integer.MAX_VALUE));
    }
}
