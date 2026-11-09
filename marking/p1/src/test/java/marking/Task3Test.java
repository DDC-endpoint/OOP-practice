package marking;

import edu.practice.parking.dto.NewHourlyPass;
import edu.practice.parking.dto.Quote;
import edu.practice.parking.error.ApiExceptionHandler;
import edu.practice.parking.error.PassNotFoundException;
import edu.practice.parking.model.HourlyPass;
import edu.practice.parking.model.ParkingPass;
import edu.practice.parking.repo.PassRepository;
import edu.practice.parking.service.ParkingService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P1 Task 3 – Service validation and HTTP errors [20 marks].
 * The error-mapping tests use a tiny throw-only controller plus YOUR ApiExceptionHandler,
 * so they don't depend on the Task 4 endpoints.
 */
class Task3Test {

    @RestController
    static class Thrower {
        @GetMapping("/boom/missing")
        public String missing() { throw new PassNotFoundException(42); }

        @GetMapping("/boom/bad")
        public String bad() { throw new IllegalArgumentException("minutes must be positive"); }
    }

    static MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(new Thrower()).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    // ---- 4 marks: quote(id, minutes) validates positive minutes and calculates the selected pass ----

    @Test
    void t3_quoteCalculatesSelectedPass_m2() {
        // FixedPass prices minutes x centsPerMinute, so this checks the service, not your Task 1 maths.
        ParkingService service = new ParkingService(new Stubs.StubRepo(
                new Stubs.FixedPass(9, "ZED-9", 3), new Stubs.FixedPass(4, "QUA-4", 5)));
        Quote q = service.quote(9, 125);
        assertEquals(9L, q.id());
        assertEquals("ZED-9", q.registration());
        assertEquals(125, q.stayMinutes());
        assertEquals(375, q.priceCents(), "125 x 3: the selected pass's own priceCents");
        assertEquals(305, service.quote(4, 61).priceCents(), "61 x 5");
    }

    @Test
    void t3_quoteRejectsNonPositiveMinutes_m2() {
        ParkingService service = new ParkingService(new PassRepository());
        assertThrows(IllegalArgumentException.class, () -> service.quote(1, 0));
        assertThrows(IllegalArgumentException.class, () -> service.quote(1, -10));
    }

    // ---- 6 marks: missing ID -> PassNotFoundException -> 404 ApiError(NOT_FOUND, ...) ----

    @Test
    void t3_missingIdThrowsPassNotFound_m2() {
        ParkingService service = new ParkingService(new PassRepository());
        assertThrows(PassNotFoundException.class, () -> service.quote(999, 60));
        assertThrows(PassNotFoundException.class, () -> service.quote(404, 1));
    }

    @Test
    void t3_passNotFoundGives404_m2() throws Exception {
        mvc().perform(get("/boom/missing")).andExpect(status().isNotFound());
    }

    @Test
    void t3_notFoundBodyIsApiError_m2() throws Exception {
        mvc().perform(get("/boom/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ---- 6 marks: addHourly validates positive unique ID, nonblank registration, positive rate; trims ----

    @Test
    void t3_addHourlyStoresTrimmedRegistration_m2() {
        PassRepository repo = new PassRepository();
        ParkingPass created = new ParkingService(repo).addHourly(new NewHourlyPass(3, "  CAR-303 ", 300));
        assertEquals(3L, created.getId());
        assertEquals("CAR-303", created.getRegistration(), "registration must be trimmed before storing");
        assertTrue(created instanceof HourlyPass, "addHourly creates an HourlyPass");
        assertEquals(300, ((HourlyPass) created).getRateCents());
        assertNotNull(repo.get(3), "the new pass must be saved in the repository");
        assertEquals("CAR-303", repo.get(3).getRegistration());
    }

    @Test
    void t3_addHourlyRejectsBadOrDuplicateId_m2() {
        PassRepository repo = new PassRepository();
        ParkingService service = new ParkingService(repo);
        int before = repo.all().size();
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(0, "CAR-1", 300)));
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(-4, "CAR-1", 300)));
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(1, "DUP-1", 999)),
                "ID 1 already exists");
        assertEquals(before, repo.all().size(), "rejected passes must not be stored");
        assertEquals("CAR-101", repo.get(1).getRegistration(), "a duplicate must not overwrite pass 1");
    }

    @Test
    void t3_addHourlyRejectsBlankRegistrationAndBadRate_m2() {
        PassRepository repo = new PassRepository();
        ParkingService service = new ParkingService(repo);
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(5, null, 300)));
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(5, "", 300)));
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(5, "   ", 300)));
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(5, "CAR-5", 0)));
        assertThrows(IllegalArgumentException.class, () -> service.addHourly(new NewHourlyPass(5, "CAR-5", -25)));
        assertNull(repo.get(5), "rejected passes must not be stored");
    }

    // ---- 4 marks: IllegalArgumentException -> HTTP 400, ApiError(BAD_REQUEST, ...) ----

    @Test
    void t3_illegalArgumentGives400_m2() throws Exception {
        mvc().perform(get("/boom/bad")).andExpect(status().isBadRequest());
    }

    @Test
    void t3_badRequestBodyIsApiError_m2() throws Exception {
        mvc().perform(get("/boom/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
