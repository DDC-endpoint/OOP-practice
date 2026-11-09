package edu.practice.d09;

import com.fasterxml.jackson.databind.JsonNode;
import edu.practice.d09.controller.PlantController;
import edu.practice.d09.service.PlantService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'PlantApiTest'.
 *
 * This is how LBP Section B is checked: the whole app starts on a random free port
 * (never 8080, so it can't clash with an app you left running), and TestRestTemplate
 * sends real HTTP requests to it, like curl would.
 *
 * POST changes the in-memory data, so @DirtiesContext restarts the app after every
 * test: each test sees the same 5 seeded plants, whatever order the tests run in.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlantApiTest {

    @Autowired
    private TestRestTemplate rest;

    // ---- helpers: one line per HTTP request ----

    private ResponseEntity<JsonNode> get(String url) {
        return rest.getForEntity(url, JsonNode.class);
    }

    private ResponseEntity<JsonNode> postPlant(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/api/plants", new HttpEntity<>(json, headers), JsonNode.class);
    }

    private static void assertStatus(int expected, ResponseEntity<JsonNode> response, String request) {
        assertEquals(expected, response.getStatusCode().value(),
                request + " should answer " + expected + " (body was: " + response.getBody() + ")");
    }

    /** The "id" of every element of a JSON array, in order. */
    private static List<Long> ids(ResponseEntity<JsonNode> response, String request) {
        assertStatus(200, response, request);
        JsonNode body = response.getBody();
        assertTrue(body != null && body.isArray(), request + " should return a JSON array, got: " + body);
        List<Long> ids = new ArrayList<>();
        body.forEach(plant -> ids.add(plant.path("id").asLong()));
        return ids;
    }

    private static void assertApiError(ResponseEntity<JsonNode> response, int status, String code, String request) {
        assertStatus(status, response, request);
        JsonNode body = response.getBody();
        assertNotNull(body, request + " should return an ApiError body");
        assertEquals(code, body.path("code").asText(), request + " should return ApiError code " + code);
        assertFalse(body.path("message").asText().isBlank(), request + " should return an ApiError message");
    }

    // ---- the tests ----

    @Test
    @DisplayName("1. GET /api/plants lists every plant (lowest id first)")
    void listAll() {
        List<Long> ids = ids(get("/api/plants"), "GET /api/plants");
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids);
        JsonNode first = get("/api/plants").getBody().get(0);
        assertEquals("Lavender", first.path("name").asText());
        assertEquals(450, first.path("priceCents").asInt());
    }

    @Test
    @DisplayName("2. GET /api/plants/{id} returns one plant (@PathVariable)")
    void getOne() {
        ResponseEntity<JsonNode> lavender = get("/api/plants/1");
        assertStatus(200, lavender, "GET /api/plants/1");
        assertEquals(1, lavender.getBody().path("id").asLong());
        assertEquals("Lavender", lavender.getBody().path("name").asText());

        ResponseEntity<JsonNode> fern = get("/api/plants/4");
        assertStatus(200, fern, "GET /api/plants/4");
        assertEquals("Fern", fern.getBody().path("name").asText());
        assertEquals(1_200, fern.getBody().path("priceCents").asInt());
        assertEquals(5, fern.getBody().path("stock").asInt());
    }

    @Test
    @DisplayName("3. GET /api/plants/{id} with an unknown id -> 404 NOT_FOUND")
    void getUnknown() {
        ResponseEntity<JsonNode> r = get("/api/plants/99");
        assertApiError(r, 404, "NOT_FOUND", "GET /api/plants/99");
        assertEquals("Plant not found: 99", r.getBody().path("message").asText());
        assertApiError(get("/api/plants/6"), 404, "NOT_FOUND", "GET /api/plants/6");
    }

    @Test
    @DisplayName("4. GET /api/plants/{id}/price?qty=... (@RequestParam)")
    void price() {
        ResponseEntity<JsonNode> r = get("/api/plants/1/price?qty=3");
        assertStatus(200, r, "GET /api/plants/1/price?qty=3");
        assertEquals(1, r.getBody().path("plantId").asLong());
        assertEquals(3, r.getBody().path("qty").asInt());
        assertEquals(1_350, r.getBody().path("totalCents").asInt());

        assertEquals(2_400, get("/api/plants/4/price?qty=2").getBody().path("totalCents").asInt(), "2 x Fern (1200)");
        assertEquals(3_999, get("/api/plants/2/price?qty=1").getBody().path("totalCents").asInt(), "1 x Olive tree (3999)");
    }

    @Test
    @DisplayName("5. price: bad or missing qty -> 400, unknown id -> 404")
    void priceErrors() {
        assertApiError(get("/api/plants/1/price?qty=0"), 400, "BAD_REQUEST", "GET /api/plants/1/price?qty=0");
        assertApiError(get("/api/plants/5/price?qty=-2"), 400, "BAD_REQUEST", "GET /api/plants/5/price?qty=-2");
        assertStatus(400, get("/api/plants/1/price"), "GET /api/plants/1/price (qty missing: it is a required @RequestParam)");
        assertApiError(get("/api/plants/99/price?qty=1"), 404, "NOT_FOUND", "GET /api/plants/99/price?qty=1");
    }

    @Test
    @DisplayName("6. GET /api/plants/search: inStockOnly is optional and defaults to false")
    void searchDefault() {
        // Basil (id 3) is sold out, but without inStockOnly it must still be listed. Cheapest first.
        assertEquals(List.of(3L, 5L, 1L), ids(get("/api/plants/search?maxPriceCents=500"),
                "GET /api/plants/search?maxPriceCents=500 (no inStockOnly)"));
        assertEquals(List.of(3L, 5L, 1L, 4L), ids(get("/api/plants/search?maxPriceCents=1200"),
                "GET /api/plants/search?maxPriceCents=1200 (no inStockOnly)"));
        assertEquals(List.of(3L, 5L, 1L), ids(get("/api/plants/search?maxPriceCents=500&inStockOnly=false"),
                "GET /api/plants/search?maxPriceCents=500&inStockOnly=false"));
        assertEquals(List.of(), ids(get("/api/plants/search?maxPriceCents=100"),
                "GET /api/plants/search?maxPriceCents=100"));
    }

    @Test
    @DisplayName("7. search?inStockOnly=true hides sold-out plants; bad or missing maxPriceCents -> 400")
    void searchInStockAndErrors() {
        assertEquals(List.of(5L, 1L), ids(get("/api/plants/search?maxPriceCents=500&inStockOnly=true"),
                "GET /api/plants/search?maxPriceCents=500&inStockOnly=true"));
        assertEquals(List.of(5L, 1L, 4L, 2L), ids(get("/api/plants/search?maxPriceCents=5000&inStockOnly=true"),
                "GET /api/plants/search?maxPriceCents=5000&inStockOnly=true"));

        assertApiError(get("/api/plants/search?maxPriceCents=0"), 400, "BAD_REQUEST", "GET /api/plants/search?maxPriceCents=0");
        assertStatus(400, get("/api/plants/search?inStockOnly=true"),
                "GET /api/plants/search?inStockOnly=true (maxPriceCents missing: it is required)");
    }

    @Test
    @DisplayName("8. POST /api/plants -> 201 Created, and the new plant shows up in later GETs")
    void postCreates() {
        ResponseEntity<JsonNode> created = postPlant("{\"id\":6,\"name\":\"Rosemary\",\"priceCents\":399,\"stock\":7}");
        assertStatus(201, created, "POST /api/plants (Rosemary)");
        assertEquals(6, created.getBody().path("id").asLong(), "POST should return the created plant");
        assertEquals("Rosemary", created.getBody().path("name").asText());

        assertEquals("Rosemary", get("/api/plants/6").getBody().path("name").asText(), "GET /api/plants/6 after the POST");
        assertEquals(List.of(3L, 5L, 6L), ids(get("/api/plants/search?maxPriceCents=400"), "search after the POST"));

        ResponseEntity<JsonNode> aloe = postPlant("{\"id\":42,\"name\":\"  Aloe vera \",\"priceCents\":899,\"stock\":0}");
        assertStatus(201, aloe, "POST /api/plants (Aloe vera)");
        assertEquals("Aloe vera", aloe.getBody().path("name").asText());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L, 42L), ids(get("/api/plants"), "GET /api/plants after two POSTs"));
    }

    @Test
    @DisplayName("9. invalid POST -> 400 BAD_REQUEST, and nothing is added")
    void invalidPostIsRejected() {
        assertApiError(postPlant("{\"id\":7,\"name\":\"   \",\"priceCents\":500,\"stock\":1}"), 400, "BAD_REQUEST", "POST with a blank name");
        assertApiError(postPlant("{\"id\":7,\"name\":\"Thyme\",\"priceCents\":0,\"stock\":1}"), 400, "BAD_REQUEST", "POST with priceCents 0");
        assertApiError(postPlant("{\"id\":7,\"name\":\"Thyme\",\"priceCents\":500,\"stock\":-1}"), 400, "BAD_REQUEST", "POST with negative stock");
        assertApiError(postPlant("{\"id\":1,\"name\":\"Fake Lavender\",\"priceCents\":100,\"stock\":1}"), 400, "BAD_REQUEST", "POST with duplicate id 1");

        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids(get("/api/plants"), "GET /api/plants after the rejected POSTs"));
        assertEquals("Lavender", get("/api/plants/1").getBody().path("name").asText(), "plant 1 must not be replaced");
        assertApiError(get("/api/plants/7"), 404, "NOT_FOUND", "GET /api/plants/7 (it was never added)");
    }

    @Test
    @DisplayName("10. the controller has the five endpoints and only talks to PlantService")
    void controllerOnlyDelegates() {
        for (Field f : PlantController.class.getDeclaredFields()) {
            assertEquals(PlantService.class, f.getType(),
                    "PlantController's only field should be the PlantService (found " + f.getType().getSimpleName() + " " + f.getName()
                            + "). The controller must not use the repository: it asks the service.");
        }
        int endpoints = 0;
        for (Method m : PlantController.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(GetMapping.class) || m.isAnnotationPresent(PostMapping.class)
                    || m.isAnnotationPresent(PutMapping.class) || m.isAnnotationPresent(DeleteMapping.class)
                    || m.isAnnotationPresent(PatchMapping.class) || m.isAnnotationPresent(RequestMapping.class)) {
                endpoints++;
            }
        }
        assertTrue(endpoints >= 5, "PlantController should have 5 endpoint methods (@GetMapping x4, @PostMapping x1), found " + endpoints);
    }
}
