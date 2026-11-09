package marking;

import edu.practice.parking.ParkingApplication;
import edu.practice.parking.dto.Quote;
import edu.practice.parking.service.ParkingService;
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

/** P1 Task 4 – Complete REST endpoints [20 marks]. Runs the real app on a random port. */
@SpringBootTest(classes = ParkingApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Task4Test {

    static final String CONTROLLER_SRC = "src/main/java/edu/practice/parking/controller/ParkingController.java";

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ParkingService service;

    // Expected values come from the app's own ParkingService, so these marks are about the
    // endpoint (mapping, params, JSON) and don't depend on your Task 1-2 arithmetic.

    // ---- 5 marks: GET /api/passes/{id}/quote?minutes=61 returns a Quote ----

    @Test
    void t4_getQuoteReturnsQuoteJson_m3() {
        ResponseEntity<Map> r = rest.getForEntity("/api/passes/1/quote?minutes=61", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        Map<?, ?> q = r.getBody();
        Quote expected = service.quote(1, 61);
        assertEquals(1, num(q.get("id")));
        assertEquals("CAR-101", q.get("registration"));
        assertEquals(61, num(q.get("stayMinutes")));
        assertEquals(expected.priceCents(), num(q.get("priceCents")));
        assertTrue(q.containsKey("passType"), "the body is the Quote record (id, registration, passType, stayMinutes, priceCents)");
    }

    @Test
    void t4_getQuoteUsesPathIdAndMinutesParam_m2() {
        for (String[] c : new String[][]{{"1", "125"}, {"1", "7"}}) {
            ResponseEntity<Map> r = rest.getForEntity("/api/passes/" + c[0] + "/quote?minutes=" + c[1], Map.class);
            assertEquals(HttpStatus.OK, r.getStatusCode(), "GET /api/passes/" + c[0] + "/quote?minutes=" + c[1]);
            Quote expected = service.quote(Long.parseLong(c[0]), Integer.parseInt(c[1]));
            assertEquals(expected.id(), num(r.getBody().get("id")));
            assertEquals(expected.stayMinutes(), num(r.getBody().get("stayMinutes")));
            assertEquals(expected.priceCents(), num(r.getBody().get("priceCents")));
        }
        assertNotEquals(HttpStatus.OK, rest.getForEntity("/api/passes/999/quote?minutes=61", Map.class).getStatusCode(),
                "the {id} path variable must select the pass (999 does not exist)");
    }

    // ---- 5 marks: GET /api/passes/quotes?minutes=61 returns sorted quotes for all passes ----

    @Test
    void t4_quotesEndpointReturnsAllPasses_m2() {
        ResponseEntity<List> r = rest.getForEntity("/api/passes/quotes?minutes=61", List.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        List<Map<String, Object>> quotes = r.getBody();
        List<?> passes = rest.getForObject("/api/passes", List.class);
        assertEquals(passes.size(), quotes.size(), "one quote per pass");
        assertEquals(service.quote(1, 61).priceCents(),
                num(quotes.stream().filter(q -> num(q.get("id")) == 1).findFirst()
                        .orElseGet(() -> fail("no quote for pass 1")).get("priceCents")));
    }

    @Test
    void t4_quotesEndpointReturnsServiceOrder_m3() {
        for (int minutes : new int[]{61, 481, 200}) {
            List<Map<String, Object>> quotes = rest.getForObject("/api/passes/quotes?minutes=" + minutes, List.class);
            List<Quote> expected = service.allQuotes(minutes);
            assertEquals(expected.stream().map(Quote::id).toList(),
                    quotes.stream().map(q -> num(q.get("id"))).toList(), "same sorted order as allQuotes(" + minutes + ")");
            assertEquals(expected.stream().map(q -> (long) q.priceCents()).toList(),
                    quotes.stream().map(q -> num(q.get("priceCents"))).toList());
        }
    }

    // ---- 5 marks: POST /api/passes/hourly accepts the JSON body and returns the created pass ----

    @Test
    void t4_postHourlyReturnsCreatedPass_m3() {
        ResponseEntity<Map> r = rest.postForEntity("/api/passes/hourly",
                Map.of("id", 3, "registration", "CAR-303", "rateCents", 300), Map.class);
        assertTrue(r.getStatusCode().is2xxSuccessful(), "POST should succeed, got " + r.getStatusCode());
        assertEquals(3, num(r.getBody().get("id")));
        assertEquals("CAR-303", r.getBody().get("registration"));
        assertEquals(300, num(r.getBody().get("rateCents")));
    }

    @Test
    void t4_postedPassIsStored_m2() {
        rest.postForEntity("/api/passes/hourly", Map.of("id", 12, "registration", "BUS-12", "rateCents", 175), Map.class);
        List<Map<String, Object>> passes = rest.getForObject("/api/passes", List.class);
        assertTrue(passes.stream().anyMatch(p -> num(p.get("id")) == 12 && "BUS-12".equals(p.get("registration"))),
                "the posted pass must appear in GET /api/passes");
    }

    // ---- 5 marks: POST returns HTTP 201; handlers delegate to the service with the correct annotations ----

    @Test
    void t4_postHourlyReturns201_m3() {
        ResponseEntity<Map> r = rest.postForEntity("/api/passes/hourly",
                Map.of("id", 3, "registration", "CAR-303", "rateCents", 300), Map.class);
        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        ResponseEntity<Map> r2 = rest.postForEntity("/api/passes/hourly",
                Map.of("id", 44, "registration", "CAR-44", "rateCents", 50), Map.class);
        assertEquals(HttpStatus.CREATED, r2.getStatusCode());
    }

    @Test
    void t4_handlersDelegateToServiceWithSpringAnnotations_m2() {
        assertEquals(HttpStatus.OK, rest.getForEntity("/api/passes/1/quote?minutes=61", Map.class).getStatusCode());
        String code = SourceScan.code(CONTROLLER_SRC);
        for (String a : new String[]{"@PathVariable", "@RequestParam", "@RequestBody"}) {
            assertTrue(code.contains(a), "controller should use " + a);
        }
        assertTrue(code.contains("@PostMapping") || code.contains("RequestMethod.POST"), "controller should map POST /hourly");
        String lower = code.toLowerCase().replaceAll("\\s+", "");
        for (String call : new String[]{"quote(", "allquotes(", "addhourly("}) {
            assertTrue(lower.contains("service." + call), "controller should delegate to the service's " + call + "...) method");
        }
        assertFalse(code.contains("PassRepository"), "the controller should not use the repository directly");
        assertFalse(code.contains("priceCents("), "pricing belongs in the model/service, not the controller");
    }

    static long num(Object o) {
        assertNotNull(o, "missing JSON field");
        return ((Number) o).longValue();
    }
}
