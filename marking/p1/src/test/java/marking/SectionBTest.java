package marking;

import edu.practice.parking.ParkingApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/** P1 Section B – six functional checks against the running app [20 marks]. */
@SpringBootTest(classes = ParkingApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SectionBTest {

    @Autowired
    TestRestTemplate rest;

    // 3 – An hourly stay of 61 minutes at 250 cents/hour produces 500 cents.
    @Test
    void b1_hourly61MinutesCosts500_m3() {
        assertEquals(500, price("/api/passes/1/quote?minutes=61"));
        assertEquals(250, price("/api/passes/1/quote?minutes=60"));
        assertEquals(1000, price("/api/passes/1/quote?minutes=200"), "200 minutes = 4 hours x 250");
    }

    // 3 – A WeekendPass stay of 481 minutes with an 800-cent flat rate produces 950 cents.
    @Test
    void b2_weekend481MinutesCosts950_m3() {
        assertEquals(950, price("/api/passes/2/quote?minutes=481"));
        assertEquals(800, price("/api/passes/2/quote?minutes=480"));
        assertEquals(1400, price("/api/passes/2/quote?minutes=700"), "220 extra minutes = 4 commenced hours: 800 + 600");
    }

    // 3 – GET /api/passes/quotes?minutes=61 returns deterministic ascending charges.
    @Test
    void b3_quotesAreDeterministicAndAscending_m3() {
        List<Map<String, Object>> first = rest.getForObject("/api/passes/quotes?minutes=61", List.class);
        assertEquals(List.of(1L, 2L), field(first, "id"), "at 61 minutes HourlyPass 1 (500) is cheaper than WeekendPass 2 (800)");
        assertAscending(first);
        assertEquals(first, rest.getForObject("/api/passes/quotes?minutes=61", List.class), "repeat calls must match");
        List<Map<String, Object>> at481 = rest.getForObject("/api/passes/quotes?minutes=481", List.class);
        assertEquals(List.of(2L, 1L), field(at481, "id"), "at 481 minutes WeekendPass 2 (950) is cheaper than HourlyPass 1 (2250)");
        assertAscending(at481);
    }

    static void assertAscending(List<Map<String, Object>> quotes) {
        List<Long> prices = field(quotes, "priceCents");
        assertEquals(prices.stream().sorted().toList(), prices, "charges must be ascending");
    }

    // 3 – A missing pass quotation returns 404 and the correct error code.
    @Test
    void b4_missingPassQuoteGives404NotFound_m3() {
        for (String url : new String[]{"/api/passes/999/quote?minutes=61", "/api/passes/77/quote?minutes=30"}) {
            ResponseEntity<Map> r = rest.getForEntity(url, Map.class);
            assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
            assertEquals("NOT_FOUND", r.getBody().get("code"));
            assertNotNull(r.getBody().get("message"));
        }
    }

    // 4 – Invalid stay minutes or invalid POST payload returns 400 without persisting a pass.
    @Test
    void b5_invalidMinutesGives400BadRequest_m2() {
        for (String url : new String[]{"/api/passes/1/quote?minutes=0", "/api/passes/2/quote?minutes=-30",
                "/api/passes/quotes?minutes=0"}) {
            ResponseEntity<Map> r = rest.getForEntity(url, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), url);
            assertEquals("BAD_REQUEST", r.getBody().get("code"), url);
        }
    }

    @Test
    void b5_invalidPostGives400AndIsNotStored_m2() {
        List<Map<String, Object>> bad = List.of(
                Map.of("id", 5, "registration", "   ", "rateCents", 300),
                Map.of("id", 6, "registration", "CAR-606", "rateCents", 0),
                Map.of("id", 0, "registration", "CAR-000", "rateCents", 300),
                Map.of("id", 1, "registration", "DUP-101", "rateCents", 999));
        for (Map<String, Object> body : bad) {
            ResponseEntity<Map> r = rest.postForEntity("/api/passes/hourly", body, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), "POST " + body);
            assertEquals("BAD_REQUEST", r.getBody().get("code"));
        }
        List<Map<String, Object>> passes = rest.getForObject("/api/passes", List.class);
        assertEquals(List.of(1L, 2L), field(passes, "id").stream().sorted().toList(), "nothing invalid may be stored");
        Map<String, Object> pass1 = passes.stream().filter(p -> ((Number) p.get("id")).longValue() == 1).findFirst().orElseThrow();
        assertEquals("CAR-101", pass1.get("registration"), "the duplicate must not replace pass 1");
    }

    // 4 – Valid POST returns 201 and the added pass appears in a later list/quote request.
    @Test
    void b6_validPostReturns201_m2() {
        ResponseEntity<Map> r = rest.postForEntity("/api/passes/hourly",
                Map.of("id", 3, "registration", "CAR-303", "rateCents", 300), Map.class);
        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertEquals(3, ((Number) r.getBody().get("id")).longValue());
    }

    @Test
    void b6_postedPassAppearsInLaterRequests_m2() {
        rest.postForEntity("/api/passes/hourly", Map.of("id", 3, "registration", "CAR-303", "rateCents", 300), Map.class);
        List<Map<String, Object>> passes = rest.getForObject("/api/passes", List.class);
        assertTrue(field(passes, "id").contains(3L), "pass 3 must be listed");
        ResponseEntity<Map> q = rest.getForEntity("/api/passes/3/quote?minutes=61", Map.class);
        assertEquals(HttpStatus.OK, q.getStatusCode(), "pass 3 can be quoted");
        assertEquals("CAR-303", q.getBody().get("registration"));
        List<Map<String, Object>> quotes = rest.getForObject("/api/passes/quotes?minutes=61", List.class);
        assertTrue(field(quotes, "id").contains(3L), "pass 3 appears in the quotes list");
        assertEquals(3, quotes.size());
    }

    long price(String url) {
        ResponseEntity<Map> r = rest.getForEntity(url, Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), url);
        return ((Number) r.getBody().get("priceCents")).longValue();
    }

    static List<Long> field(List<Map<String, Object>> rows, String name) {
        return rows.stream().map(m -> ((Number) m.get(name)).longValue()).toList();
    }
}
