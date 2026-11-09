package edu.practice.d10;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'CinemaApiTest'.
 *
 * These are Section B style functional checks: the whole app starts on a random free port
 * and each test sends real HTTP requests to it, like you would with curl, then compares
 * what came back with the values the "paper" (LESSON.md, Your task) says it should be.
 *
 * Most checks already pass: that is what a working API looks like. A few fail, because the
 * app has bugs. Read each failure as a SYMPTOM ("expected 404 but was 500"), not as a
 * pointer to a line. Finding the line is your job.
 *
 * POST changes the in-memory data, so @DirtiesContext restarts the app after every test:
 * each test sees the same 6 seeded screenings, whatever order the tests run in.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CinemaApiTest {

    @Autowired
    private TestRestTemplate rest;

    // ---- helpers: one line per HTTP request ----

    private ResponseEntity<JsonNode> get(String url) {
        return rest.getForEntity(url, JsonNode.class);
    }

    private ResponseEntity<JsonNode> postScreening(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/api/screenings", new HttpEntity<>(json, headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> addScreening(long id, String film, int runtimeMinutes, int basePriceCents) {
        String json = "{\"id\":" + id + ",\"film\":\"" + film + "\",\"runtimeMinutes\":" + runtimeMinutes
                + ",\"basePriceCents\":" + basePriceCents + "}";
        ResponseEntity<JsonNode> r = postScreening(json);
        assertStatus(201, r, "POST /api/screenings " + json);
        return r;
    }

    private static void assertStatus(int expected, ResponseEntity<JsonNode> response, String request) {
        assertEquals(expected, response.getStatusCode().value(),
                request + " should answer " + expected + " (body was: " + response.getBody() + ")");
    }

    /** GET /api/screenings/{id}/quote?seats=..., checked against the expected seat price and total. */
    private void assertQuote(long id, int seats, int seatPriceCents, int totalCents, String why) {
        String url = "/api/screenings/" + id + "/quote?seats=" + seats;
        ResponseEntity<JsonNode> r = get(url);
        assertStatus(200, r, "GET " + url);
        JsonNode q = r.getBody();
        assertEquals(id, q.path("screeningId").asLong(), "GET " + url + ": screeningId");
        assertEquals(seats, q.path("seats").asInt(), "GET " + url + ": seats");
        assertEquals(seatPriceCents, q.path("seatPriceCents").asInt(), "GET " + url + ": seatPriceCents (" + why + ")");
        assertEquals(totalCents, q.path("totalCents").asInt(), "GET " + url + ": totalCents (" + why + ")");
    }

    /** The given field of every element of a JSON array, in order. */
    private static List<Long> longs(ResponseEntity<JsonNode> response, String field, String request) {
        assertStatus(200, response, request);
        JsonNode body = response.getBody();
        assertTrue(body != null && body.isArray(), request + " should return a JSON array, got: " + body);
        List<Long> values = new ArrayList<>();
        body.forEach(item -> values.add(item.path(field).asLong()));
        return values;
    }

    private static void assertApiError(ResponseEntity<JsonNode> response, int status, String code, String request) {
        assertStatus(status, response, request);
        JsonNode body = response.getBody();
        assertNotNull(body, request + " should return an ApiError body");
        assertEquals(code, body.path("code").asText(), request + " should return ApiError code " + code + " (body was: " + body + ")");
        assertFalse(body.path("message").asText().isBlank(), request + " should return an ApiError message");
    }

    // ---- the checks ----

    @Test
    @DisplayName("1. GET /api/screenings lists every screening, lowest id first")
    void listAll() {
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), longs(get("/api/screenings"), "id", "GET /api/screenings"));
        JsonNode first = get("/api/screenings").getBody().get(0);
        assertEquals("The Last Lighthouse", first.path("film").asText());
        assertEquals(95, first.path("runtimeMinutes").asInt());
        assertEquals(900, first.path("basePriceCents").asInt());
        JsonNode last = get("/api/screenings").getBody().get(5);
        assertEquals("Glass Harbour", last.path("film").asText());
    }

    @Test
    @DisplayName("2. GET /api/screenings/{id} returns one screening")
    void getOne() {
        ResponseEntity<JsonNode> orbit = get("/api/screenings/2");
        assertStatus(200, orbit, "GET /api/screenings/2");
        assertEquals("Orbit of Ash", orbit.getBody().path("film").asText());
        assertEquals(150, orbit.getBody().path("runtimeMinutes").asInt());
        assertEquals(1_000, orbit.getBody().path("basePriceCents").asInt());

        ResponseEntity<JsonNode> ferry = get("/api/screenings/5");
        assertStatus(200, ferry, "GET /api/screenings/5");
        assertEquals(5, ferry.getBody().path("id").asLong());
        assertEquals("Midnight Ferry", ferry.getBody().path("film").asText());
        assertEquals(200, ferry.getBody().path("runtimeMinutes").asInt());
    }

    @Test
    @DisplayName("3. an unknown screening id -> 404 NOT_FOUND (GET and quote)")
    void unknownId() {
        ResponseEntity<JsonNode> r = get("/api/screenings/99");
        assertApiError(r, 404, "NOT_FOUND", "GET /api/screenings/99");
        assertEquals("Screening not found: 99", r.getBody().path("message").asText());

        assertApiError(get("/api/screenings/0"), 404, "NOT_FOUND", "GET /api/screenings/0");
        ResponseEntity<JsonNode> q = get("/api/screenings/42/quote?seats=2");
        assertApiError(q, 404, "NOT_FOUND", "GET /api/screenings/42/quote?seats=2");
        assertEquals("Screening not found: 42", q.getBody().path("message").asText());
    }

    @Test
    @DisplayName("4. quote: a film of 120 minutes or less pays the base price per seat")
    void quoteShortFilms() {
        assertQuote(1, 2, 900, 1_800, "95 minutes: no surcharge");
        assertQuote(4, 3, 1_100, 3_300, "104 minutes: no surcharge");
        addScreening(20, "Short Cut", 120, 750);
        assertQuote(20, 4, 750, 3_000, "exactly 120 minutes: still no surcharge");
    }

    @Test
    @DisplayName("5. quote: a longer film pays 150 cents per started 30 minutes over 120")
    void quoteLongFilms() {
        assertQuote(3, 2, 1_100, 2_200, "128 minutes: 8 over -> 1 block");
        assertQuote(5, 1, 1_450, 1_450, "200 minutes: 80 over -> 3 blocks");
        addScreening(21, "Long Way Round", 151, 1_000);
        assertQuote(21, 2, 1_300, 2_600, "151 minutes: 31 over -> 2 blocks");
        addScreening(22, "Night Bus", 121, 500);
        assertQuote(22, 5, 650, 3_250, "121 minutes: 1 over -> 1 block");
    }

    @Test
    @DisplayName("6. quote: more long-film checks (150, 180 and 210 minutes)")
    void quoteMoreLongFilms() {
        assertQuote(2, 1, 1_150, 1_150, "Orbit of Ash, 150 minutes");
        assertQuote(6, 2, 700, 1_400, "Glass Harbour, 180 minutes");
        addScreening(23, "Harbour Lights Saga", 210, 1_000);
        assertQuote(23, 1, 1_450, 1_450, "Harbour Lights Saga, 210 minutes");
    }

    @Test
    @DisplayName("7. quote: seats outside 1..10, or missing -> 400 BAD_REQUEST")
    void badSeats() {
        assertApiError(get("/api/screenings/1/quote?seats=0"), 400, "BAD_REQUEST", "GET /api/screenings/1/quote?seats=0");
        assertApiError(get("/api/screenings/3/quote?seats=11"), 400, "BAD_REQUEST", "GET /api/screenings/3/quote?seats=11");
        assertApiError(get("/api/screenings/5/quote?seats=-3"), 400, "BAD_REQUEST", "GET /api/screenings/5/quote?seats=-3");
        assertApiError(get("/api/screenings/quotes?seats=12"), 400, "BAD_REQUEST", "GET /api/screenings/quotes?seats=12");
        assertStatus(400, get("/api/screenings/1/quote"), "GET /api/screenings/1/quote (seats missing: it is required)");
        assertQuote(4, 10, 1_100, 11_000, "10 seats is the most allowed");
    }

    @Test
    @DisplayName("8. GET /api/screenings/quotes: most expensive first, equal totals by lowest id")
    void quotesOrder() {
        ResponseEntity<JsonNode> one = get("/api/screenings/quotes");
        assertEquals(List.of(5L, 2L, 3L, 4L, 1L, 6L), longs(one, "screeningId", "GET /api/screenings/quotes (seats defaults to 1)"));
        assertEquals(1, one.getBody().get(0).path("seats").asInt(), "seats should default to 1");

        ResponseEntity<JsonNode> three = get("/api/screenings/quotes?seats=3");
        assertEquals(List.of(5L, 2L, 3L, 4L, 1L, 6L), longs(three, "screeningId", "GET /api/screenings/quotes?seats=3"));
        List<Long> totals = longs(three, "totalCents", "GET /api/screenings/quotes?seats=3");
        for (int i = 1; i < totals.size(); i++) {
            assertTrue(totals.get(i - 1) >= totals.get(i), "totals should never go UP down the list: " + totals);
        }

        addScreening(7, "Quiet Storm", 110, 900);       // same seat price as screening 1
        addScreening(12, "Tidewater", 140, 1_300);      // same seat price as screening 5
        assertEquals(List.of(5L, 12L, 2L, 3L, 4L, 1L, 7L, 6L),
                longs(get("/api/screenings/quotes?seats=2"), "screeningId", "GET /api/screenings/quotes?seats=2 after adding 7 and 12"));
    }

    @Test
    @DisplayName("9. POST /api/screenings -> 201 Created, and the new screening shows up afterwards")
    void postCreates() {
        ResponseEntity<JsonNode> created = postScreening("{\"id\":7,\"film\":\"  Quiet Storm \",\"runtimeMinutes\":133,\"basePriceCents\":1000}");
        assertStatus(201, created, "POST /api/screenings (Quiet Storm)");
        assertEquals(7, created.getBody().path("id").asLong(), "POST should return the created screening");
        assertEquals("Quiet Storm", created.getBody().path("film").asText(), "the film name should be trimmed");

        assertEquals("Quiet Storm", get("/api/screenings/7").getBody().path("film").asText(), "GET /api/screenings/7 after the POST");
        assertQuote(7, 2, 1_150, 2_300, "133 minutes: 13 over -> 1 block");

        addScreening(30, "Lantern Season", 90, 600);
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 30L), longs(get("/api/screenings"), "id", "GET /api/screenings after two POSTs"));
    }

    @Test
    @DisplayName("10. invalid POST -> 400 BAD_REQUEST, and nothing is added")
    void invalidPost() {
        assertApiError(postScreening("{\"id\":7,\"film\":\"   \",\"runtimeMinutes\":100,\"basePriceCents\":900}"), 400, "BAD_REQUEST", "POST with a blank film");
        assertApiError(postScreening("{\"id\":7,\"film\":\"Echo\",\"runtimeMinutes\":0,\"basePriceCents\":900}"), 400, "BAD_REQUEST", "POST with runtimeMinutes 0");
        assertApiError(postScreening("{\"id\":7,\"film\":\"Echo\",\"runtimeMinutes\":100,\"basePriceCents\":-5}"), 400, "BAD_REQUEST", "POST with a negative basePriceCents");
        assertApiError(postScreening("{\"id\":0,\"film\":\"Echo\",\"runtimeMinutes\":100,\"basePriceCents\":900}"), 400, "BAD_REQUEST", "POST with id 0");
        assertApiError(postScreening("{\"id\":2,\"film\":\"Fake Orbit\",\"runtimeMinutes\":100,\"basePriceCents\":900}"), 400, "BAD_REQUEST", "POST with duplicate id 2");

        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), longs(get("/api/screenings"), "id", "GET /api/screenings after the rejected POSTs"));
        assertEquals("Orbit of Ash", get("/api/screenings/2").getBody().path("film").asText(), "screening 2 must not be replaced");
    }
}
