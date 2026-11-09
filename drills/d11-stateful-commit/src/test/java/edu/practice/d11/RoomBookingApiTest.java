package edu.practice.d11;

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
 * Run me: right-click this file in IntelliJ -> Run 'RoomBookingApiTest'.
 *
 * Section B style: the whole app starts on a random free port and each test sends real HTTP
 * requests to it, like curl would. Run BookingServiceTest first: it tells you more precisely
 * what is wrong. These checks pass once the service is right.
 *
 * Commits change the in-memory data, so @DirtiesContext restarts the app after every test:
 * each test sees the same 4 seeded bookings and no used keys, whatever order the tests run in.
 *
 *   id 1  BIRCH [13,15)      id 2  ASPEN [14,16)      id 3  ASPEN [9,11)      id 4  CEDAR [10,12)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoomBookingApiTest {

    @Autowired
    private TestRestTemplate rest;

    // ---- helpers: one line per HTTP request ----

    private ResponseEntity<JsonNode> post(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity(url, new HttpEntity<>(json, headers), JsonNode.class);
    }

    private ResponseEntity<JsonNode> preview(String json) {
        return post("/api/rooms/preview", json);
    }

    private ResponseEntity<JsonNode> commit(String json) {
        return post("/api/rooms/commit", json);
    }

    private static void assertStatus(int expected, ResponseEntity<JsonNode> response, String request) {
        assertEquals(expected, response.getStatusCode().value(),
                request + " should answer " + expected + " (body was: " + response.getBody() + ")");
    }

    private static void assertApiError(ResponseEntity<JsonNode> response, int status, String code, String request) {
        assertStatus(status, response, request);
        JsonNode body = response.getBody();
        assertNotNull(body, request + " should return an ApiError body");
        assertEquals(code, body.path("code").asText(), request + " should return ApiError code " + code + " (body was: " + body + ")");
        assertFalse(body.path("message").asText().isBlank(), request + " should return an ApiError message");
    }

    /** GET /api/rooms/bookings as "id ROOM start-end" strings, in the order the API returned them. */
    private List<String> bookings() {
        ResponseEntity<JsonNode> r = rest.getForEntity("/api/rooms/bookings", JsonNode.class);
        assertStatus(200, r, "GET /api/rooms/bookings");
        assertTrue(r.getBody() != null && r.getBody().isArray(), "GET /api/rooms/bookings should return a JSON array, got: " + r.getBody());
        List<String> out = new ArrayList<>();
        r.getBody().forEach(b -> out.add(b.path("id").asLong() + " " + slot(b)));
        return out;
    }

    /** "ROOM start-end" for one booking / request JSON object. */
    private static String slot(JsonNode n) {
        return n.path("room").asText() + " " + n.path("start").asInt() + "-" + n.path("end").asInt();
    }

    /** Every element of a JSON array field as "ROOM start-end" (plus " REASON" when the element has one). */
    private static List<String> slots(JsonNode body, String field) {
        JsonNode array = body.path(field);
        assertTrue(array.isArray(), "the body should have a \"" + field + "\" array, got: " + body);
        List<String> out = new ArrayList<>();
        array.forEach(n -> out.add(slot(n) + (n.has("reason") ? " " + n.path("reason").asText() : "")));
        return out;
    }

    private static final List<String> SEEDED = List.of("3 ASPEN 9-11", "2 ASPEN 14-16", "1 BIRCH 13-15", "4 CEDAR 10-12");

    // ---- the checks ----

    @Test
    @DisplayName("1. GET /api/rooms/bookings: room A to Z, then start, then id")
    void listSeeded() {
        assertEquals(SEEDED, bookings(), "the 4 seeded bookings in room, start, id order");
        JsonNode first = rest.getForEntity("/api/rooms/bookings", JsonNode.class).getBody().get(0);
        assertEquals(3, first.path("id").asLong());
        assertEquals("ASPEN", first.path("room").asText());
        assertEquals(9, first.path("start").asInt());
        assertEquals(11, first.path("end").asInt());
    }

    @Test
    @DisplayName("2. POST /api/rooms/preview: accepted and rejected requests, with reasons (half-open boundaries)")
    void previewDecides() {
        ResponseEntity<JsonNode> r = preview("{\"bookings\":["
                + "{\"room\":\"BIRCH\",\"start\":15,\"end\":17},"
                + "{\"room\":\"BIRCH\",\"start\":14,\"end\":16},"
                + "{\"room\":\"CEDAR\",\"start\":9,\"end\":10},"
                + "{\"room\":\"CEDAR\",\"start\":9,\"end\":11},"
                + "{\"room\":\"ASPEN\",\"start\":12,\"end\":14},"
                + "{\"room\":\"ASPEN\",\"start\":13,\"end\":14}]}");
        assertStatus(200, r, "POST /api/rooms/preview");
        assertEquals(List.of("BIRCH 15-17", "CEDAR 9-10", "ASPEN 12-14"), slots(r.getBody(), "accepted"),
                "BIRCH [15,17) and CEDAR [9,10) only TOUCH confirmed bookings, so they are accepted");
        assertEquals(List.of("BIRCH 14-16 CLASHES_WITH_CONFIRMED", "CEDAR 9-11 CLASHES_WITH_CONFIRMED",
                        "ASPEN 13-14 CLASHES_WITH_EARLIER_REQUEST"), slots(r.getBody(), "rejected"));
    }

    @Test
    @DisplayName("3. preview twice: GET /api/rooms/bookings is unchanged, and both previews agree")
    void previewTwiceChangesNothing() {
        String json = "{\"bookings\":[{\"room\":\"ASPEN\",\"start\":11,\"end\":14},{\"room\":\"BIRCH\",\"start\":8,\"end\":13}]}";
        ResponseEntity<JsonNode> first = preview(json);
        assertStatus(200, first, "first POST /api/rooms/preview");
        assertEquals(List.of("ASPEN 11-14", "BIRCH 8-13"), slots(first.getBody(), "accepted"));
        assertEquals(SEEDED, bookings(), "GET /api/rooms/bookings after one preview");

        ResponseEntity<JsonNode> second = preview(json);
        assertStatus(200, second, "second POST /api/rooms/preview");
        assertEquals(first.getBody(), second.getBody(), "the same preview twice must give the same answer");
        assertEquals(SEEDED, bookings(), "GET /api/rooms/bookings after two previews: a preview must never save");
    }

    @Test
    @DisplayName("4. POST /api/rooms/commit with a new key saves the accepted bookings")
    void commitSaves() {
        ResponseEntity<JsonNode> r = commit("{\"key\":\"standup-1\",\"bookings\":["
                + "{\"room\":\"ASPEN\",\"start\":11,\"end\":12},"
                + "{\"room\":\"ASPEN\",\"start\":10,\"end\":11},"
                + "{\"room\":\"CEDAR\",\"start\":12,\"end\":15}]}");
        assertStatus(200, r, "POST /api/rooms/commit (standup-1)");
        assertEquals("standup-1", r.getBody().path("key").asText());
        List<String> booked = new ArrayList<>();
        r.getBody().path("booked").forEach(b -> booked.add(b.path("id").asLong() + " " + slot(b)));
        assertEquals(List.of("5 ASPEN 11-12", "6 CEDAR 12-15"), booked, "booked: new ids 5 and 6, in request order");
        assertEquals(List.of("ASPEN 10-11 CLASHES_WITH_CONFIRMED"), slots(r.getBody(), "rejected"));

        assertEquals(List.of("3 ASPEN 9-11", "5 ASPEN 11-12", "2 ASPEN 14-16", "1 BIRCH 13-15", "4 CEDAR 10-12", "6 CEDAR 12-15"),
                bookings(), "GET /api/rooms/bookings after the commit");

        ResponseEntity<JsonNode> r2 = commit("{\"key\":\"standup-2\",\"bookings\":[{\"room\":\"ASPEN\",\"start\":11,\"end\":12}]}");
        assertStatus(200, r2, "POST /api/rooms/commit (standup-2)");
        assertEquals(List.of("ASPEN 11-12 CLASHES_WITH_CONFIRMED"), slots(r2.getBody(), "rejected"),
                "ASPEN [11,12) is now confirmed, so a new key can't book it again");
        assertEquals(0, r2.getBody().path("booked").size());
        assertEquals(6, bookings().size());
    }

    @Test
    @DisplayName("5. same key + same request: the same answer again, and nothing new is saved")
    void retryIsIdempotent() {
        ResponseEntity<JsonNode> first = commit("{\"key\":\"retro-42\",\"bookings\":["
                + "{\"room\":\"BIRCH\",\"start\":15,\"end\":17},{\"room\":\"CEDAR\",\"start\":12,\"end\":13}]}");
        assertStatus(200, first, "POST /api/rooms/commit (retro-42)");
        assertEquals(6, bookings().size(), "4 seeded + 2 committed");

        // The same request, written differently: other field order, extra spaces. It's still EQUAL once parsed.
        String sameAgain = "{ \"bookings\": [ {\"end\":17, \"start\":15, \"room\":\"BIRCH\"}, "
                + "{\"room\":\"CEDAR\", \"end\":13, \"start\":12} ], \"key\": \"retro-42\" }";
        ResponseEntity<JsonNode> retry = commit(sameAgain);
        assertStatus(200, retry, "retry of POST /api/rooms/commit (retro-42)");
        assertEquals(first.getBody(), retry.getBody(), "a retry must return exactly the first answer (same ids)");
        assertEquals(6, bookings().size(), "a retry must not book anything again");

        assertStatus(200, commit("{\"key\":\"retro-43\",\"bookings\":[{\"room\":\"ASPEN\",\"start\":16,\"end\":17}]}"),
                "POST /api/rooms/commit (retro-43)");
        assertEquals(7, bookings().size());
        ResponseEntity<JsonNode> later = commit(sameAgain);
        assertEquals(first.getBody(), later.getBody(), "even after another commit, the retry gets the ORIGINAL answer");
        assertEquals(7, bookings().size());
    }

    @Test
    @DisplayName("6. same key + a different request -> 409 CONFLICT, and nothing is saved")
    void reusedKeyConflicts() {
        String original = "{\"key\":\"board-7\",\"bookings\":[{\"room\":\"BIRCH\",\"start\":15,\"end\":17}]}";
        ResponseEntity<JsonNode> first = commit(original);
        assertStatus(200, first, "POST /api/rooms/commit (board-7)");
        List<String> after = bookings();
        assertEquals(5, after.size());

        assertApiError(commit("{\"key\":\"board-7\",\"bookings\":[{\"room\":\"BIRCH\",\"start\":15,\"end\":18}]}"),
                409, "CONFLICT", "board-7 again with different hours");
        assertApiError(commit("{\"key\":\"board-7\",\"bookings\":[{\"room\":\"BIRCH\",\"start\":15,\"end\":17},"
                + "{\"room\":\"ASPEN\",\"start\":12,\"end\":13}]}"), 409, "CONFLICT", "board-7 again with an extra booking");
        assertEquals(after, bookings(), "a 409 must not save anything");

        ResponseEntity<JsonNode> retry = commit(original);
        assertStatus(200, retry, "board-7 with the ORIGINAL request");
        assertEquals(first.getBody(), retry.getBody(), "the original request still gets its stored answer");
    }

    @Test
    @DisplayName("7. bad hours or an empty list -> 400 BAD_REQUEST, and nothing is saved")
    void badHours() {
        assertApiError(preview("{\"bookings\":[{\"room\":\"ASPEN\",\"start\":7,\"end\":9}]}"), 400, "BAD_REQUEST",
                "preview ASPEN [7,9) (opens at 8)");
        assertApiError(preview("{\"bookings\":[{\"room\":\"ASPEN\",\"start\":12,\"end\":12}]}"), 400, "BAD_REQUEST",
                "preview ASPEN [12,12) (empty interval)");
        assertApiError(preview("{\"bookings\":[]}"), 400, "BAD_REQUEST", "preview with no bookings");
        assertApiError(commit("{\"key\":\"late-1\",\"bookings\":[{\"room\":\"CEDAR\",\"start\":16,\"end\":19}]}"), 400, "BAD_REQUEST",
                "commit CEDAR [16,19) (closes at 18)");
        assertApiError(commit("{\"key\":\"late-2\",\"bookings\":[{\"room\":\"ASPEN\",\"start\":11,\"end\":12},"
                + "{\"room\":\"BIRCH\",\"start\":14,\"end\":13}]}"), 400, "BAD_REQUEST", "commit with BIRCH [14,13) (end before start)");
        assertEquals(SEEDED, bookings(), "nothing may be saved, not even the valid ASPEN [11,12) in the same commit");
    }

    @Test
    @DisplayName("8. an unknown room -> 404 NOT_FOUND; a blank key -> 400; nothing is saved")
    void unknownRoomAndBlankKey() {
        ResponseEntity<JsonNode> r = preview("{\"bookings\":[{\"room\":\"ZEUS\",\"start\":9,\"end\":10}]}");
        assertApiError(r, 404, "NOT_FOUND", "preview room ZEUS");
        assertEquals("Room not found: ZEUS", r.getBody().path("message").asText());

        ResponseEntity<JsonNode> c = commit("{\"key\":\"x-1\",\"bookings\":[{\"room\":\"BIRCH\",\"start\":8,\"end\":9},"
                + "{\"room\":\"ORION\",\"start\":9,\"end\":10}]}");
        assertApiError(c, 404, "NOT_FOUND", "commit with room ORION");
        assertEquals("Room not found: ORION", c.getBody().path("message").asText());

        assertApiError(commit("{\"key\":\"  \",\"bookings\":[{\"room\":\"BIRCH\",\"start\":8,\"end\":9}]}"), 400, "BAD_REQUEST",
                "commit with a blank key");
        assertApiError(commit("{\"bookings\":[{\"room\":\"BIRCH\",\"start\":8,\"end\":9}]}"), 400, "BAD_REQUEST",
                "commit with no key");
        assertEquals(SEEDED, bookings(), "nothing may be saved");
    }
}
