package marking;

import edu.practice.parking.dto.Quote;
import edu.practice.parking.model.ParkingPass;
import edu.practice.parking.repo.PassRepository;
import edu.practice.parking.service.ParkingService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P1 Task 2 – Create WeekendPass and polymorphic quotes [22 marks].
 * Paper: Weekend 480 -> 800, 481 -> 950; at 481 WeekendPass 2 (950) comes before HourlyPass 1 (2250).
 * WeekendPass is a class YOU create, so it is reached through Reflect.
 */
class Task2Test {

    static final String WEEKEND = "edu.practice.parking.model.WeekendPass";
    static final String SERVICE_SRC = "src/main/java/edu/practice/parking/service/ParkingService.java";

    static ParkingPass weekend(long id, String registration, int flatCents) {
        Object o = Reflect.construct(WEEKEND, id, registration, flatCents);
        assertTrue(o instanceof ParkingPass, "WeekendPass must extend ParkingPass");
        return (ParkingPass) o;
    }

    // ---- 6 marks: WeekendPass extends ParkingPass, (long id, String registration, int flatCents), getFlatCents() ----

    @Test
    void t2_weekendPassExtendsParkingPass_m2() {
        Class<?> c = Reflect.require(WEEKEND);
        assertTrue(Reflect.extendsClass(c, ParkingPass.class), "WeekendPass must extend ParkingPass");
        assertFalse(Modifier.isAbstract(c.getModifiers()), "WeekendPass must be a concrete class");
    }

    @Test
    void t2_weekendPassConstructorChainsIdAndRegistration_m2() {
        ParkingPass w = weekend(2, "VAN-202", 800);
        assertEquals(2L, w.getId());
        assertEquals("VAN-202", w.getRegistration());
        ParkingPass other = weekend(77, "BUS-77", 1234);
        assertEquals(77L, other.getId());
        assertEquals("BUS-77", other.getRegistration());
    }

    @Test
    void t2_weekendPassGetFlatCents_m2() {
        assertEquals(800, Reflect.call(weekend(2, "VAN-202", 800), "getFlatCents"));
        assertEquals(1234, Reflect.call(weekend(77, "BUS-77", 1234), "getFlatCents"));
    }

    // ---- 8 marks: flat for 1-480 minutes; +150 per commenced extra hour above 480; reject non-positive ----

    @Test
    void t2_weekendFlatRateUpTo480Minutes_m2() {
        ParkingPass w = weekend(2, "VAN-202", 800);
        assertEquals(800, w.priceCents(480), "paper: 480 minutes -> 800 cents");
        assertEquals(800, w.priceCents(1));
        assertEquals(800, w.priceCents(240));
        assertEquals(1000, weekend(8, "W-8", 1000).priceCents(480));
    }

    @Test
    void t2_weekendAddsPerCommencedExtraHour_m4() {
        ParkingPass w = weekend(2, "VAN-202", 800);
        assertEquals(950, w.priceCents(481), "paper: 481 minutes -> 950 cents");
        assertEquals(950, w.priceCents(540), "480 + 60 minutes is still 1 extra hour");
        assertEquals(1100, w.priceCents(541), "541 minutes = 2 commenced extra hours");
        assertEquals(1100, w.priceCents(600));
        // 1000 minutes: 520 extra minutes = 9 commenced hours -> 500 + 9 x 150 = 1850
        assertEquals(1850, weekend(9, "W-9", 500).priceCents(1000));
    }

    @Test
    void t2_weekendRejectsNonPositiveStay_m2() {
        ParkingPass w = weekend(2, "VAN-202", 800);
        assertThrows(IllegalArgumentException.class, () -> w.priceCents(0));
        assertThrows(IllegalArgumentException.class, () -> w.priceCents(-5));
    }

    // ---- 8 marks: ParkingService.allQuotes(minutes) – polymorphic, price ASC then ID ASC, no type checks ----

    @Test
    void t2_allQuotesAt481PutsWeekendFirst_m2() {
        // Uses the repository seed: HourlyPass(1,"CAR-101",250) and WeekendPass(2,"VAN-202",800).
        PassRepository repo = new PassRepository();
        List<Quote> quotes = new ParkingService(repo).allQuotes(481);
        assertEquals(List.of(2L, 1L), ids(quotes),
                "paper: at 481 minutes WeekendPass 2 (950) appears before HourlyPass 1 (2250). Seed WeekendPass 2 in PassRepository");
        assertEquals(950, quotes.get(0).priceCents());
        assertEquals(repo.get(1).priceCents(481), quotes.get(1).priceCents(), "HourlyPass 1 quoted with its own priceCents");
    }

    @Test
    void t2_allQuotesPricesEachPassWithItsOwnRule_m2() {
        // FixedPass is a third subclass (minutes x 2): only polymorphic calls price all three correctly.
        PassRepository repo = new Stubs.StubRepo(new Stubs.FixedPass(10, "F-10", 2), weekend(11, "W-11", 300));
        List<Quote> quotes = new ParkingService(repo).allQuotes(61);
        assertEquals(2, quotes.size(), "one quote per pass");
        Quote f = find(quotes, 10);
        assertEquals(122, f.priceCents(), "FixedPass 10: 61 x 2");
        assertEquals("F-10", f.registration());
        assertEquals(61, f.stayMinutes());
        Quote w = find(quotes, 11);
        assertEquals(300, w.priceCents(), "WeekendPass 11: flat 300");
        assertEquals("W-11", w.registration());
        assertEquals(61, w.stayMinutes());
        assertEquals(List.of(10L, 11L), ids(quotes), "122 before 300");
        assertEquals(List.of(11L, 10L), ids(new ParkingService(repo).allQuotes(700)), "at 700: W-11 = 300 + 4 x 150 = 900 < F-10 = 1400");
    }

    @Test
    void t2_allQuotesSortsByPriceThenIdAscending_m2() {
        // Inserted out of order; three passes tie at 400 cents for 100 minutes.
        PassRepository repo = new Stubs.StubRepo(
                new Stubs.FixedPass(33, "T-33", 4),
                new Stubs.FixedPass(17, "T-17", 4),
                new Stubs.FixedPass(2, "T-2", 8),
                weekend(5, "T-5", 400),
                new Stubs.FixedPass(1, "T-1", 1));
        List<Quote> quotes = new ParkingService(repo).allQuotes(100);
        assertEquals(List.of(1L, 5L, 17L, 33L, 2L), ids(quotes), "price ASC (100, 400, 400, 400, 800), ties by ID ASC");
        assertEquals(List.of(100, 400, 400, 400, 800), prices(quotes));
    }

    @Test
    void t2_allQuotesWithoutTypeChecks_m2() {
        PassRepository repo = new Stubs.StubRepo(new Stubs.FixedPass(4, "F-4", 3), weekend(6, "W-6", 100));
        assertEquals(List.of(6L, 4L), ids(new ParkingService(repo).allQuotes(60)), "W-6 = 100, F-4 = 180");
        SourceScan.assertNoTypeChecks(SERVICE_SRC, "HourlyPass", "WeekendPass");
    }

    static List<Long> ids(List<Quote> quotes) {
        return quotes.stream().map(Quote::id).toList();
    }

    static List<Integer> prices(List<Quote> quotes) {
        return quotes.stream().map(Quote::priceCents).toList();
    }

    static Quote find(List<Quote> quotes, long id) {
        return quotes.stream().filter(q -> q.id() == id).findFirst()
                .orElseGet(() -> fail("no quote for pass " + id));
    }
}
