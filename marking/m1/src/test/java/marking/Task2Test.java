package marking;

import edu.practice.courier.dto.Quote;
import edu.practice.courier.dto.VanLoad;
import edu.practice.courier.model.Parcel;
import edu.practice.courier.repo.ParcelRepository;
import edu.practice.courier.service.ParcelService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M1 Task 2 – Create ExpressParcel and load the van [22 marks].
 * Paper: ExpressParcel 3 (900 g, 400 p, signature) -> 1,060 p, priority 3; ExpressParcel 4 (300 g, 350 p) -> 530 p, priority 2.
 *        loadVan(2000) -> loaded [3,4,2], skipped [1], 1,700 g used, 300 g left, 1,840 p.
 * ExpressParcel is a class YOU create, so it is reached through Reflect.
 */
class Task2Test {

    static final String EXPRESS = "edu.practice.courier.model.ExpressParcel";
    static final String SERVICE_SRC = "src/main/java/edu/practice/courier/service/ParcelService.java";

    static Parcel express(long id, String code, int weightGrams, int basePence, boolean signatureRequired) {
        Object o = Reflect.construct(EXPRESS, id, code, weightGrams, basePence, signatureRequired);
        assertTrue(o instanceof Parcel, "ExpressParcel must extend Parcel");
        return (Parcel) o;
    }

    static int price(int weightGrams, int basePence, boolean signatureRequired) {
        return express(9, "E-9", weightGrams, basePence, signatureRequired).pricePence();
    }

    static void assertRejected(int weightGrams, int basePence, boolean signatureRequired, String what) {
        assertThrows(IllegalArgumentException.class,
                () -> express(9, "E-9", weightGrams, basePence, signatureRequired).pricePence(),
                what + " must throw IllegalArgumentException");
    }

    // ---- 7 marks: ExpressParcel extends Parcel; (long, String, int, int basePence, boolean signatureRequired); getters ----

    @Test
    void t2_expressParcelExtendsParcel_m2() {
        Class<?> c = Reflect.require(EXPRESS);
        assertTrue(Reflect.extendsClass(c, Parcel.class), "ExpressParcel must extend Parcel");
        assertFalse(Modifier.isAbstract(c.getModifiers()), "ExpressParcel must be a concrete class");
    }

    @Test
    void t2_expressConstructorChainsToParcel_m2() {
        Parcel e = express(3, "CP-1003", 900, 400, true);
        assertEquals(3L, e.getId());
        assertEquals("CP-1003", e.getTrackingCode());
        assertEquals(900, e.getWeightGrams());
        Parcel other = express(61, "EX-61", 4321, 0, false);
        assertEquals(61L, other.getId());
        assertEquals("EX-61", other.getTrackingCode());
        assertEquals(4321, other.getWeightGrams());
    }

    @Test
    void t2_expressFinalFieldsAndGetters_m3() {
        Class<?> c = Reflect.require(EXPRESS);
        for (String[] f : new String[][]{{"basePence", "int"}, {"signatureRequired", "boolean"}}) {
            Field field;
            try {
                field = c.getDeclaredField(f[0]);
            } catch (NoSuchFieldException e) {
                fail("Declare a field 'private final " + f[1] + " " + f[0] + "' in ExpressParcel");
                return;
            }
            assertEquals(f[1], field.getType().getName(), f[0] + " type");
            assertTrue(Modifier.isFinal(field.getModifiers()), f[0] + " must be final");
            assertTrue(Modifier.isPrivate(field.getModifiers()), f[0] + " must be private");
        }
        Parcel e = express(3, "CP-1003", 900, 400, true);
        assertEquals(400, Reflect.call(e, "getBasePence"));
        assertEquals(true, Reflect.call(e, "isSignatureRequired"));
        Parcel other = express(4, "CP-1004", 300, 350, false);
        assertEquals(350, Reflect.call(other, "getBasePence"));
        assertEquals(false, Reflect.call(other, "isSignatureRequired"));
    }

    // ---- 7 marks: pricePence() = basePence + 90 per started 250 g (+300 if signature); priority 3/2; validation ----

    @Test
    void t2_expressPricePaperExamples_m2() {
        assertEquals(1060, express(3, "CP-1003", 900, 400, true).pricePence(), "paper: 400 + 4 x 90 + 300");
        assertEquals(530, express(4, "CP-1004", 300, 350, false).pricePence(), "paper: 350 + 2 x 90");
    }

    @Test
    void t2_expressPriceRoundsUpAndAddsSignature_m2() {
        assertEquals(90, price(250, 0, false), "exactly 250 g is one block");
        assertEquals(180, price(251, 0, false), "251 g starts a second block");
        assertEquals(580, price(251, 100, true), "100 + 2 x 90 + 300");
        assertEquals(390, price(1, 0, true), "1 g is one started block");
        assertEquals(1260, price(1000, 900, false), "900 + 4 x 90");
    }

    @Test
    void t2_expressPriorityDependsOnSignature_m1() {
        assertEquals(3, express(3, "CP-1003", 900, 400, true).priority());
        assertEquals(2, express(4, "CP-1004", 300, 350, false).priority());
        assertEquals(3, express(12, "E-12", 5, 0, true).priority());
    }

    @Test
    void t2_expressRejectsInvalidValuesAndOverflow_m2() {
        assertRejected(0, 400, true, "weightGrams 0");
        assertRejected(-10, 400, false, "weightGrams -10");
        assertRejected(900, -1, false, "basePence -1");
        assertRejected(Integer.MAX_VALUE, 2_000_000_000, false, "a price above Integer.MAX_VALUE");
        // MAX weight = 8,589,935 blocks x 90 = 773,094,150; this base + signature lands exactly on Integer.MAX_VALUE.
        assertEquals(Integer.MAX_VALUE, price(Integer.MAX_VALUE, 1_374_389_197, true), "exactly Integer.MAX_VALUE still fits");
        assertRejected(Integer.MAX_VALUE, 1_374_389_198, true, "one penny over Integer.MAX_VALUE");
    }

    // ---- 8 marks: ParcelService.loadVan(capacityGrams) ----

    @Test
    void t2_loadVanPaperExample_m2() {
        VanLoad v = new ParcelService(new ParcelRepository()).loadVan(2000);
        assertEquals(List.of(3L, 4L, 2L), ids(v.loaded()),
                "paper: loaded [3,4,2]. Seed ExpressParcel 3 and 4 in ParcelRepository after Task 2");
        assertEquals(List.of(1L), v.skipped());
        assertEquals(2000, v.capacityGrams());
        assertEquals(1700, v.weightUsed());
        assertEquals(300, v.weightRemaining());
        assertEquals(1840, v.totalPence());
    }

    @Test
    void t2_loadVanSortsPriorityDescPriceAscIdAsc_m2() {
        // Inserted out of order. Same priority -> cheaper first; same priority and price -> lower ID first.
        ParcelService s = new ParcelService(new Stubs.StubRepo(
                new Stubs.FixedParcel(30, "A-30", 100, 500, 1),
                new Stubs.FixedParcel(7, "B-7", 100, 500, 1),
                new Stubs.FixedParcel(12, "C-12", 100, 200, 1),
                new Stubs.FixedParcel(50, "D-50", 100, 900, 4),
                new Stubs.FixedParcel(2, "E-2", 100, 100, 2),
                new Stubs.FixedParcel(41, "F-41", 100, 50, 2)));
        VanLoad v = s.loadVan(10_000);
        assertEquals(List.of(50L, 41L, 2L, 12L, 7L, 30L), ids(v.loaded()), "priority DESC, then pricePence ASC, then ID ASC");
        assertEquals(List.of(900, 50, 100, 200, 500, 500), v.loaded().stream().map(Quote::pricePence).toList());
        assertEquals(List.of(), v.skipped());
        assertEquals(2250, v.totalPence());
    }

    @Test
    void t2_loadVanGreedyKeepsGoingAfterASkip_m2() {
        ParcelService s = new ParcelService(new Stubs.StubRepo(
                new Stubs.FixedParcel(5, "T-5", 200, 6, 1),
                new Stubs.FixedParcel(2, "Q-2", 500, 10, 2),
                new Stubs.FixedParcel(4, "S-4", 100, 5, 1),
                new Stubs.FixedParcel(1, "P-1", 600, 10, 3),
                new Stubs.FixedParcel(3, "R-3", 300, 20, 2)));
        // order 1(600) 2(500) 3(300) 4(100) 5(200)
        VanLoad full = s.loadVan(1000);
        assertEquals(List.of(1L, 3L, 4L), ids(full.loaded()), "2 does not fit after 1, but 3 and 4 still do (exact fit allowed)");
        assertEquals(List.of(2L, 5L), full.skipped(), "skipped IDs in the order they were considered");
        assertEquals(1000, full.weightUsed());
        assertEquals(0, full.weightRemaining());
        assertEquals(35, full.totalPence());
        VanLoad small = s.loadVan(700);
        assertEquals(List.of(1L, 4L), ids(small.loaded()));
        assertEquals(List.of(2L, 3L, 5L), small.skipped());
        assertEquals(700, small.weightUsed());
        assertEquals(15, small.totalPence());
    }

    @Test
    void t2_loadVanIsPolymorphicWithoutTypeChecks_m2() {
        // FixedParcel is a third subclass: only calls through the Parcel reference price all three correctly.
        ParcelService s = new ParcelService(new Stubs.StubRepo(
                new Stubs.FixedParcel(10, "F-10", 1000, 200, 1),
                express(11, "E-11", 250, 50, false),
                new Stubs.FixedParcel(12, "F-12", 50, 999, 5)));
        VanLoad v = s.loadVan(1100);
        assertEquals(List.of(12L, 11L), ids(v.loaded()));
        assertEquals(List.of(999, 140), v.loaded().stream().map(Quote::pricePence).toList(), "E-11 = 50 + 90");
        assertEquals(List.of(5, 2), v.loaded().stream().map(Quote::priority).toList());
        assertEquals(List.of(10L), v.skipped());
        assertEquals("E-11", v.loaded().get(1).trackingCode());
        assertEquals(250, v.loaded().get(1).weightGrams());
        SourceScan.assertNoTypeChecks(SERVICE_SRC, "StandardParcel", "ExpressParcel");
    }

    static List<Long> ids(List<Quote> quotes) {
        return quotes.stream().map(Quote::id).toList();
    }
}
