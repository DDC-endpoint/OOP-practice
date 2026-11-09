package edu.practice.d02;

import edu.practice.d02.model.Membership;
import edu.practice.d02.repo.MembershipRepository;
import edu.practice.testkit.Reflect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/** You create the class edu.practice.d02.model.Annual. These tests find it by name. */
class AnnualTest {

    static final String ANNUAL = "edu.practice.d02.model.Annual";

    static Object annual(long id, String member, int yearlyCents) {
        return Reflect.construct(ANNUAL, id, member, yearlyCents);
    }

    @Test
    @DisplayName("1. class Annual exists and extends Membership")
    void annualExtendsMembership() {
        Class<?> type = Reflect.require(ANNUAL);
        assertTrue(Reflect.extendsClass(type, Membership.class), "Annual must extend Membership");
    }

    @Test
    @DisplayName("2. yearlyCents is a private final int field with a getter")
    void fieldAndGetter() throws Exception {
        Class<?> type = Reflect.require(ANNUAL);
        Field f;
        try {
            f = type.getDeclaredField("yearlyCents");
        } catch (NoSuchFieldException e) {
            fail("Annual needs a field called yearlyCents");
            return;
        }
        assertTrue(Modifier.isPrivate(f.getModifiers()) && Modifier.isFinal(f.getModifiers()), "yearlyCents should be private final");
        assertEquals(int.class, f.getType());
        assertEquals(39_000, Reflect.call(annual(2, "Ben", 39_000), "getYearlyCents"));
        assertEquals(12_345, Reflect.call(annual(5, "Cai", 12_345), "getYearlyCents"));
    }

    @Test
    @DisplayName("3. constructor chains id and member to the parent")
    void chainsToParent() {
        Membership m = (Membership) annual(2, "  Ben ", 39_000);
        assertEquals(2L, m.getId());
        assertEquals("Ben", m.getMember());
        assertThrows(IllegalArgumentException.class, () -> annual(0, "Ben", 39_000), "the parent should still reject id 0");
    }

    @Test
    @DisplayName("4. monthly fee = yearly / 12, rounded UP to a whole cent")
    void monthlyFeeRoundsUp() {
        assertEquals(3_250, ((Membership) annual(2, "Ben", 39_000)).monthlyFeeCents(10));   // exact
        assertEquals(1_029, ((Membership) annual(3, "Dee", 12_345)).monthlyFeeCents(3));    // 1028.75 -> 1029
        assertEquals(1, ((Membership) annual(4, "Eli", 1)).monthlyFeeCents(0));             // 0.08 -> 1
    }

    @Test
    @DisplayName("5. the number of visits does not change an Annual fee")
    void visitsDoNotMatter() {
        Membership m = (Membership) annual(2, "Ben", 39_000);
        assertEquals(m.monthlyFeeCents(0), m.monthlyFeeCents(30));
    }

    @Test
    @DisplayName("6. rejects yearlyCents <= 0 and negative visits")
    void validation() {
        assertThrows(IllegalArgumentException.class, () -> annual(2, "Ben", 0));
        assertThrows(IllegalArgumentException.class, () -> annual(2, "Ben", -100));
        Membership m = (Membership) annual(2, "Ben", 39_000);
        assertThrows(IllegalArgumentException.class, () -> m.monthlyFeeCents(-1));
    }

    @Test
    @DisplayName("7. the repository seeds Annual #2 for Ben")
    void repositorySeed() {
        Membership m = new MembershipRepository().get(2L);
        assertNotNull(m, "add new Annual(2, \"Ben\", 39_000) to the MembershipRepository constructor");
        assertEquals("Annual", m.getClass().getSimpleName());
        assertEquals(3_250, m.monthlyFeeCents(4));
    }
}
