package marking;

import edu.practice.courier.dto.NewStandardParcel;
import edu.practice.courier.dto.Quote;
import edu.practice.courier.error.ApiExceptionHandler;
import edu.practice.courier.error.DuplicateParcelException;
import edu.practice.courier.error.ParcelNotFoundException;
import edu.practice.courier.model.Parcel;
import edu.practice.courier.model.StandardParcel;
import edu.practice.courier.repo.ParcelRepository;
import edu.practice.courier.service.ParcelService;
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
 * M1 Task 3 – Service lookup, validation and HTTP errors [20 marks].
 * The error-mapping tests use a tiny throw-only controller plus YOUR ApiExceptionHandler,
 * so they don't depend on the Task 4 endpoints.
 */
class Task3Test {

    @RestController
    static class Thrower {
        @GetMapping("/boom/missing")
        public String missing() { throw new ParcelNotFoundException(42); }

        @GetMapping("/boom/bad")
        public String bad() { throw new IllegalArgumentException("capacityGrams must be positive"); }

        @GetMapping("/boom/duplicate")
        public String duplicate() { throw new DuplicateParcelException("Tracking code already exists: CP-1001"); }
    }

    static MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(new Thrower()).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    // ---- 4 marks: quote(id) builds a Quote from the parcel's own rules; unknown ID -> ParcelNotFoundException ----

    @Test
    void t3_quoteUsesTheParcelsOwnPriceAndPriority_m2() {
        // FixedParcel has a chosen price and priority, so this checks the service, not your Task 1 maths.
        ParcelService service = new ParcelService(new Stubs.StubRepo(
                new Stubs.FixedParcel(9, "ZED-9", 700, 375, 4), new Stubs.FixedParcel(4, "QUA-4", 50, 305, 1)));
        Quote q = service.quote(9);
        assertEquals(9L, q.id());
        assertEquals("ZED-9", q.trackingCode());
        assertEquals(700, q.weightGrams());
        assertEquals(4, q.priority());
        assertEquals(375, q.pricePence());
        Quote other = service.quote(4);
        assertEquals(305, other.pricePence());
        assertEquals(1, other.priority());
        assertEquals("QUA-4", other.trackingCode());
    }

    @Test
    void t3_unknownIdThrowsParcelNotFound_m2() {
        ParcelService service = new ParcelService(new ParcelRepository());
        assertThrows(ParcelNotFoundException.class, () -> service.quote(999));
        assertThrows(ParcelNotFoundException.class, () -> service.quote(0));
        assertThrows(ParcelNotFoundException.class, () -> service.quote(-7));
    }

    // ---- 6 marks: addStandard validates and trims, then saves a StandardParcel ----

    @Test
    void t3_addStandardSavesTrimmedParcel_m2() {
        ParcelRepository repo = new ParcelRepository();
        Parcel created = new ParcelService(repo).addStandard(new NewStandardParcel(5, "  CP-1005 ", 2600, 150));
        assertTrue(created instanceof StandardParcel, "addStandard creates a StandardParcel");
        assertEquals(5L, created.getId());
        assertEquals("CP-1005", created.getTrackingCode(), "the tracking code must be trimmed before storing");
        assertEquals(2600, created.getWeightGrams());
        assertEquals(150, Reflect.call(created, "getRatePerBlockPence"));
        assertNotNull(repo.find(5), "the new parcel must be saved in the repository");
        assertEquals("CP-1005", repo.find(5).getTrackingCode());
        // range limits are inclusive
        ParcelService service = new ParcelService(repo);
        assertEquals(6L, service.addStandard(new NewStandardParcel(6, "MAX-6", 30_000, 10_000)).getId());
        assertEquals(7L, service.addStandard(new NewStandardParcel(7, "MIN-7", 1, 1)).getId());
        assertNotNull(repo.find(7));
    }

    @Test
    void t3_addStandardRejectsBadIdAndBlankCode_m2() {
        ParcelRepository repo = new ParcelRepository();
        ParcelService service = new ParcelService(repo);
        int before = repo.all().size();
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(0, "CP-2000", 500, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(-4, "CP-2000", 500, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, null, 500, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "", 500, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "   ", 500, 100)));
        assertEquals(before, repo.all().size(), "rejected parcels must not be stored");
        assertNull(repo.find(8));
    }

    @Test
    void t3_addStandardRejectsOutOfRangeWeightAndRate_m2() {
        ParcelRepository repo = new ParcelRepository();
        ParcelService service = new ParcelService(repo);
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "W-8", 0, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "W-8", -5, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "W-8", 30_001, 100)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "W-8", 500, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.addStandard(new NewStandardParcel(8, "W-8", 500, 10_001)));
        assertNull(repo.find(8), "rejected parcels must not be stored");
    }

    // ---- 4 marks: duplicate ID or tracking code (ignoring case and spaces) -> DuplicateParcelException ----

    @Test
    void t3_duplicateIdThrowsDuplicateParcel_m2() {
        ParcelRepository repo = new ParcelRepository();
        ParcelService service = new ParcelService(repo);
        assertThrows(DuplicateParcelException.class, () -> service.addStandard(new NewStandardParcel(1, "CP-7777", 500, 100)));
        assertThrows(DuplicateParcelException.class, () -> service.addStandard(new NewStandardParcel(2, "CP-8888", 900, 120)));
        assertEquals("CP-1001", repo.find(1).getTrackingCode(), "a duplicate must not replace parcel 1");
        assertEquals("CP-1002", repo.find(2).getTrackingCode(), "a duplicate must not replace parcel 2");
    }

    @Test
    void t3_duplicateTrackingCodeIgnoringCaseThrowsDuplicateParcel_m2() {
        ParcelRepository repo = new ParcelRepository();
        ParcelService service = new ParcelService(repo);
        assertThrows(DuplicateParcelException.class, () -> service.addStandard(new NewStandardParcel(8, " cp-1002 ", 500, 100)));
        assertThrows(DuplicateParcelException.class, () -> service.addStandard(new NewStandardParcel(9, "Cp-1001", 500, 100)));
        assertNull(repo.find(8), "a duplicate tracking code must not be stored");
        assertNull(repo.find(9));
        service.addStandard(new NewStandardParcel(10, "CP-1010", 500, 100));
        assertThrows(DuplicateParcelException.class, () -> service.addStandard(new NewStandardParcel(11, "cp-1010", 500, 100)),
                "codes added through addStandard count too");
    }

    // ---- 6 marks: loadVan rejects capacity <= 0; handler maps 400 / 404 / 409 to ApiError ----

    @Test
    void t3_loadVanRejectsNonPositiveCapacity_m1() {
        ParcelService service = new ParcelService(new Stubs.StubRepo(new Stubs.FixedParcel(1, "F-1", 100, 10, 1)));
        assertThrows(IllegalArgumentException.class, () -> service.loadVan(0));
        assertThrows(IllegalArgumentException.class, () -> service.loadVan(-250));
    }

    @Test
    void t3_illegalArgumentGives400BadRequest_m1() throws Exception {
        mvc().perform(get("/boom/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void t3_parcelNotFoundGives404NotFound_m2() throws Exception {
        mvc().perform(get("/boom/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void t3_duplicateGives409Conflict_m2() throws Exception {
        mvc().perform(get("/boom/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
