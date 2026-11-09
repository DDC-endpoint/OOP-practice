package edu.practice.d06;

import edu.practice.d06.dto.RoomQuote;
import edu.practice.d06.dto.StayRequest;
import edu.practice.d06.model.Room;
import edu.practice.d06.model.StandardRoom;
import edu.practice.d06.repo.RoomRepository;
import edu.practice.d06.service.QuoteService;
import edu.practice.testkit.Reflect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'RoomQuoteTest'.
 *
 * You fill in the record RoomQuote yourself, so these tests reach its constructor and accessors
 * through the Reflect helper. That way this file compiles before your record has any components.
 *
 * The repository starts with:
 *   #1 "101" STANDARD sleeps 2,  9,000 per night
 *   #2 "102" STANDARD sleeps 4, 12,500 per night
 *   #3 "301" SUITE    sleeps 4, 11,000 per night + 4,000 service fee per stay
 *   #4 "302" SUITE    sleeps 6, 15,000 per night + 4,000 service fee per stay
 *   #5 "103" STANDARD sleeps 1,  6,500 per night
 */
class RoomQuoteTest {

    static final String ROOM_QUOTE = "edu.practice.d06.dto.RoomQuote";

    private final RoomRepository repo = new RoomRepository();
    private final QuoteService service = new QuoteService(repo);

    /** new RoomQuote(roomId, number, type, nights, totalCents), through Reflect. */
    private static Object rq(long roomId, String number, String type, int nights, int totalCents) {
        return Reflect.construct(ROOM_QUOTE, roomId, number, type, nights, totalCents);
    }

    private static List<Object> ids(List<RoomQuote> quotes) {
        List<Object> ids = new ArrayList<>();
        for (RoomQuote q : quotes) ids.add(Reflect.call(q, "roomId"));
        return ids;
    }

    private static List<Object> totals(List<RoomQuote> quotes) {
        List<Object> totals = new ArrayList<>();
        for (RoomQuote q : quotes) totals.add(Reflect.call(q, "totalCents"));
        return totals;
    }

    // ---------------------------------------------------------------- the record itself

    @Test
    @DisplayName("1. RoomQuote is a record: (long roomId, String number, String type, int nights, int totalCents)")
    void recordShape() {
        assertTrue(RoomQuote.class.isRecord(), "RoomQuote must be a record");
        RecordComponent[] parts = RoomQuote.class.getRecordComponents();
        List<String> names = new ArrayList<>();
        List<Class<?>> types = new ArrayList<>();
        for (RecordComponent c : parts) {
            names.add(c.getName());
            types.add(c.getType());
        }
        // Jackson turns each component into a JSON field with the SAME name, in this order.
        assertEquals(List.of("roomId", "number", "type", "nights", "totalCents"), names,
                "the component names are the JSON field names, so they must match exactly");
        assertEquals(List.of(long.class, String.class, String.class, int.class, int.class), types);
    }

    @Test
    @DisplayName("2. accessors are roomId(), number()... (not getRoomId()); equals and toString come for free")
    void accessorsEqualsToString() {
        Object suite = rq(3, "301", "SUITE", 3, 37_000);
        assertEquals(3L, Reflect.call(suite, "roomId"));
        assertEquals("301", Reflect.call(suite, "number"));
        assertEquals("SUITE", Reflect.call(suite, "type"));
        assertEquals(3, Reflect.call(suite, "nights"));
        assertEquals(37_000, Reflect.call(suite, "totalCents"));

        Object single = rq(5, "103", "STANDARD", 2, 13_000);
        assertEquals(5L, Reflect.call(single, "roomId"));
        assertEquals(13_000, Reflect.call(single, "totalCents"));

        assertEquals(rq(3, "301", "SUITE", 3, 37_000), suite, "two records with the same values are equal");
        assertEquals(suite.hashCode(), rq(3, "301", "SUITE", 3, 37_000).hashCode());
        assertNotEquals(rq(3, "301", "SUITE", 3, 36_999), suite);
        assertEquals("RoomQuote[roomId=5, number=103, type=STANDARD, nights=2, totalCents=13000]", single.toString());
    }

    @Test
    @DisplayName("3. the compact constructor rejects a blank number, nights <= 0 and a negative total")
    void compactConstructorValidates() {
        assertNotNull(rq(1, "101", "STANDARD", 1, 9_000), "good data must still build a RoomQuote");
        assertDoesNotThrow(() -> rq(1, "101", "STANDARD", 1, 0), "a total of 0 (a free night) is allowed");
        assertThrows(IllegalArgumentException.class, () -> rq(1, "  ", "STANDARD", 1, 9_000));
        assertThrows(IllegalArgumentException.class, () -> rq(1, null, "STANDARD", 1, 9_000));
        assertThrows(IllegalArgumentException.class, () -> rq(1, "101", "STANDARD", 0, 9_000));
        assertThrows(IllegalArgumentException.class, () -> rq(1, "101", "STANDARD", -2, 9_000));
        assertThrows(IllegalArgumentException.class, () -> rq(1, "101", "STANDARD", 1, -1));
    }

    // ---------------------------------------------------------------- the service

    @Test
    @DisplayName("4. quote(id, nights) builds a RoomQuote using that room's own price rule")
    void singleQuote() {
        assertEquals(rq(3, "301", "SUITE", 2, 26_000), service.quote(3, 2));        // 2 x 11,000 + 4,000
        assertEquals(rq(1, "101", "STANDARD", 5, 45_000), service.quote(1, 5));     // 5 x 9,000
        assertEquals(rq(4, "302", "SUITE", 1, 19_000), service.quote(4, 1));        // 15,000 + 4,000
    }

    @Test
    @DisplayName("5. quote rejects an unknown room and nights <= 0")
    void singleQuoteValidation() {
        assertThrows(IllegalArgumentException.class, () -> service.quote(99, 2));
        assertThrows(IllegalArgumentException.class, () -> service.quote(0, 1));
        assertThrows(IllegalArgumentException.class, () -> service.quote(1, 0));
    }

    @Test
    @DisplayName("6. QuoteService turns a Room into a RoomQuote in ONE private helper method")
    void privateHelper() {
        boolean found = false;
        for (Method m : QuoteService.class.getDeclaredMethods()) {
            if (Modifier.isPrivate(m.getModifiers())
                    && m.getReturnType() == RoomQuote.class
                    && Arrays.asList(m.getParameterTypes()).contains(Room.class)) {
                found = true;
            }
        }
        assertTrue(found, "add a helper like: private RoomQuote toQuote(Room room, int nights) { ... }");
    }

    @Test
    @DisplayName("7. quotes(request): only rooms big enough, cheapest first")
    void quotesFilterAndSort() {
        assertEquals(List.of(
                rq(3, "301", "SUITE", 3, 37_000),
                rq(2, "102", "STANDARD", 3, 37_500),
                rq(4, "302", "SUITE", 3, 49_000)
        ), service.quotes(new StayRequest(3, 3)));

        List<RoomQuote> solo = service.quotes(new StayRequest(2, 1));
        assertEquals(List.of(5L, 1L, 2L, 3L, 4L), ids(solo), "1 guest fits in every room");
        assertEquals(List.of(13_000, 18_000, 25_000, 26_000, 34_000), totals(solo));

        assertEquals(List.of(2L, 3L, 4L), ids(service.quotes(new StayRequest(1, 4))),
                "a room that sleeps exactly 4 is big enough for 4 guests");
        assertEquals(List.of(), service.quotes(new StayRequest(1, 7)), "no room sleeps 7");
    }

    @Test
    @DisplayName("8. equal totals -> lower room id first")
    void tieBreakOnId() {
        repo.save(new StandardRoom(16, "116", 2, 9_000));   // same price as room 101 for any stay
        List<RoomQuote> oneNight = service.quotes(new StayRequest(1, 2));
        assertEquals(List.of(1L, 16L, 2L, 3L, 4L), ids(oneNight));
        assertEquals(List.of(9_000, 9_000, 12_500, 15_000, 19_000), totals(oneNight));

        List<RoomQuote> fourNights = service.quotes(new StayRequest(4, 2));
        assertEquals(List.of(1L, 16L, 3L, 2L, 4L), ids(fourNights));
        assertEquals(List.of(36_000, 36_000, 48_000, 50_000, 64_000), totals(fourNights));
    }
}
