package edu.practice.d08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'ApiExceptionHandlerTest'.
 *
 * MockMvc sends fake HTTP requests through the whole Spring app (controller -> service ->
 * your ApiExceptionHandler) without opening a real port. Each test checks the status code
 * AND the JSON body {"code": ..., "message": ...}.
 *
 * Bookings change the in-memory data, so @DirtiesContext gives every test a fresh app
 * (fresh repository with the 3 seeded tickets). Test order doesn't matter.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    // ---- small helpers so the tests read like the HTTP requests they send ----

    private ResultActions send(MockHttpServletRequestBuilder request) {
        try {
            return mvc.perform(request);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            fail("Nothing handled " + cause.getClass().getSimpleName() + " (\"" + cause.getMessage() + "\"). "
                    + "It escaped to Spring, which would answer 500 Internal Server Error. "
                    + "Add an @ExceptionHandler(" + cause.getClass().getSimpleName() + ".class) method to ApiExceptionHandler.");
            return null;
        }
    }

    private ResultActions get(String url) {
        return send(MockMvcRequestBuilders.get(url));
    }

    private ResultActions post(String url, String json) {
        return send(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Status, JSON content type, and the two ApiError fields. */
    private void expectError(ResultActions response, int status, String code, String message) throws Exception {
        response.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").value(message));
    }

    // ---- 400 ----

    @Test
    @DisplayName("1. IllegalArgumentException -> 400 BAD_REQUEST (booking without a customer)")
    void blankCustomerIs400() throws Exception {
        expectError(post("/api/tickets/1/book", "{\"customer\":\"   \"}"), 400, "BAD_REQUEST", "customer is required");
        expectError(post("/api/tickets/2/book", "{}"), 400, "BAD_REQUEST", "customer is required");

        get("/api/tickets/1").andExpect(status().isOk()).andExpect(jsonPath("$.heldBy", nullValue()));
    }

    @Test
    @DisplayName("2. IllegalArgumentException -> 400 BAD_REQUEST (adding an invalid ticket)")
    void invalidNewTicketIs400() throws Exception {
        expectError(post("/api/tickets", "{\"id\":10,\"event\":\"Opera Gala\",\"seat\":\"C3\",\"priceCents\":0}"),
                400, "BAD_REQUEST", "priceCents must be positive");
        expectError(post("/api/tickets", "{\"id\":-1,\"event\":\"Opera Gala\",\"seat\":\"C3\",\"priceCents\":4500}"),
                400, "BAD_REQUEST", "id must be positive");
        post("/api/tickets", "{\"id\":2,\"event\":\"Poetry Slam\",\"seat\":\"Z9\",\"priceCents\":900}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("already exists")));

        get("/api/tickets").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
    }

    // ---- 404 ----

    @Test
    @DisplayName("3. TicketNotFoundException -> 404 NOT_FOUND (GET an unknown id)")
    void unknownGetIs404() throws Exception {
        expectError(get("/api/tickets/99"), 404, "NOT_FOUND", "Ticket not found: 99");
        expectError(get("/api/tickets/12345"), 404, "NOT_FOUND", "Ticket not found: 12345");
    }

    @Test
    @DisplayName("4. TicketNotFoundException -> 404 NOT_FOUND (booking an unknown id)")
    void unknownBookIs404() throws Exception {
        expectError(post("/api/tickets/77/book", "{\"customer\":\"Ana\"}"), 404, "NOT_FOUND", "Ticket not found: 77");
        expectError(post("/api/tickets/4/book", "{\"customer\":\"Ben\"}"), 404, "NOT_FOUND", "Ticket not found: 4");
    }

    // ---- 409 ----

    @Test
    @DisplayName("5. TicketConflictException -> 409 CONFLICT (seat already held by someone else)")
    void heldSeatIs409() throws Exception {
        expectError(post("/api/tickets/3/book", "{\"customer\":\"Ana\"}"), 409, "CONFLICT", "Ticket 3 is already booked");

        get("/api/tickets/3").andExpect(status().isOk()).andExpect(jsonPath("$.heldBy").value("Kofi"));
    }

    @Test
    @DisplayName("6. TicketConflictException -> 409 CONFLICT (booking the same seat twice)")
    void doubleBookingIs409() throws Exception {
        post("/api/tickets/2/book", "{\"customer\":\"Ana\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heldBy").value("Ana"));

        expectError(post("/api/tickets/2/book", "{\"customer\":\"Ben\"}"), 409, "CONFLICT", "Ticket 2 is already booked");
        expectError(post("/api/tickets/2/book", "{\"customer\":\"Ana\"}"), 409, "CONFLICT", "Ticket 2 is already booked");

        get("/api/tickets/2").andExpect(jsonPath("$.heldBy").value("Ana"));
    }
}
