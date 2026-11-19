package marking;

import edu.practice.courier.CourierApplication;
import edu.practice.courier.model.NewStandardParcel;
import edu.practice.courier.model.Quote;
import edu.practice.courier.model.VanLoad;
import edu.practice.courier.service.ParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * M1 Task 4 – REST endpoints [20 marks].
 * ParcelService is replaced by a Mockito mock, so these marks are about YOUR controller only
 * (mapping, @PathVariable, @RequestParam default, @RequestBody, 201) and not your Task 1-3 logic.
 */
@SpringBootTest(classes = CourierApplication.class)
@AutoConfigureMockMvc
class Task4Test {

    static final String CONTROLLER_SRC = "src/main/java/edu/practice/courier/controller/ParcelController.java";
    static final String NEW_PARCEL_JSON =
            "{\"id\":5,\"trackingCode\":\"CP-1005\",\"weightGrams\":2600,\"ratePerBlockPence\":150}";

    @Autowired
    MockMvc mvc;

    @MockBean
    ParcelService service;

    /** NewStandardParcel is a plain class (no equals), so match the JSON body field by field. */
    static NewStandardParcel bodyIs(long id, String trackingCode, int weightGrams, int ratePerBlockPence) {
        return argThat(b -> b != null && b.getId() == id && trackingCode.equals(b.getTrackingCode())
                && b.getWeightGrams() == weightGrams && b.getRatePerBlockPence() == ratePerBlockPence);
    }

    static VanLoad van(int capacity) {
        return new VanLoad(capacity, 1200, capacity - 1200, 1590,
                List.of(new Quote(3, "CP-1003", 900, 3, 1060), new Quote(4, "CP-1004", 300, 2, 530)), List.of(2L, 1L));
    }

    // ---- 5 marks: GET /parcels/{id}/quote ----

    @Test
    void t4_getQuoteReturnsQuoteJson_m3() throws Exception {
        when(service.quote(3)).thenReturn(new Quote(3, "CP-1003", 900, 3, 1060));
        mvc.perform(get("/parcels/3/quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.trackingCode").value("CP-1003"))
                .andExpect(jsonPath("$.weightGrams").value(900))
                .andExpect(jsonPath("$.priority").value(3))
                .andExpect(jsonPath("$.pricePence").value(1060));
    }

    @Test
    void t4_getQuoteUsesThePathVariable_m2() throws Exception {
        when(service.quote(42)).thenReturn(new Quote(42, "Z-42", 10, 1, 77));
        mvc.perform(get("/parcels/42/quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.pricePence").value(77));
        verify(service).quote(42L);
    }

    // ---- 6 marks: POST /parcels/standard with a NewStandardParcel body -> 201 + created parcel ----

    @Test
    void t4_postStandardReturns201Created_m3() throws Exception {
        when(service.addStandard(any())).thenReturn(new Stubs.FixedParcel(5, "CP-1005", 2600, 900, 1));
        mvc.perform(post("/parcels/standard").contentType(MediaType.APPLICATION_JSON).content(NEW_PARCEL_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void t4_postStandardPassesBodyAndReturnsCreatedParcel_m3() throws Exception {
        when(service.addStandard(any())).thenReturn(new Stubs.FixedParcel(5, "CP-1005", 2600, 900, 1));
        mvc.perform(post("/parcels/standard").contentType(MediaType.APPLICATION_JSON).content(NEW_PARCEL_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.trackingCode").value("CP-1005"))
                .andExpect(jsonPath("$.weightGrams").value(2600));
        verify(service).addStandard(bodyIs(5, "CP-1005", 2600, 150));
    }

    // ---- 6 marks: GET /parcels/van?capacityGrams=... (defaults to 2000) ----

    @Test
    void t4_vanPassesCapacityParamAndReturnsVanLoad_m3() throws Exception {
        when(service.loadVan(1500)).thenReturn(van(1500));
        mvc.perform(get("/parcels/van").param("capacityGrams", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacityGrams").value(1500))
                .andExpect(jsonPath("$.weightUsed").value(1200))
                .andExpect(jsonPath("$.weightRemaining").value(300))
                .andExpect(jsonPath("$.totalPence").value(1590))
                .andExpect(jsonPath("$.loaded[0].id").value(3))
                .andExpect(jsonPath("$.loaded[1].pricePence").value(530))
                .andExpect(jsonPath("$.skipped[0]").value(2))
                .andExpect(jsonPath("$.skipped[1]").value(1));
        verify(service).loadVan(1500);
    }

    @Test
    void t4_vanCapacityDefaultsTo2000_m3() throws Exception {
        when(service.loadVan(2000)).thenReturn(van(2000));
        mvc.perform(get("/parcels/van"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacityGrams").value(2000));
        verify(service).loadVan(2000);
    }

    // ---- 3 marks: every handler delegates to the service (no business logic in the controller) ----

    @Test
    void t4_controllerDelegatesAllLogicToService_m3() throws Exception {
        when(service.quote(8)).thenReturn(new Quote(8, "D-8", 1, 1, 1));
        when(service.loadVan(750)).thenReturn(van(750));
        when(service.addStandard(any())).thenReturn(new Stubs.FixedParcel(6, "D-6", 100, 1, 1));
        mvc.perform(get("/parcels/8/quote")).andExpect(status().isOk());
        mvc.perform(get("/parcels/van?capacityGrams=750")).andExpect(status().isOk());
        mvc.perform(post("/parcels/standard").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":6,\"trackingCode\":\"D-6\",\"weightGrams\":100,\"ratePerBlockPence\":1}"))
                .andExpect(status().is2xxSuccessful());
        verify(service).quote(8L);
        verify(service).loadVan(750);
        verify(service).addStandard(bodyIs(6, "D-6", 100, 1));
        SourceScan.assertNotMentioned(CONTROLLER_SRC, "Business logic belongs in ParcelService; the controller only delegates.",
                "ParcelRepository", "pricePence(", "priority(", "Comparator", "sorted(", "new StandardParcel", "throw new");
    }
}
