package marking;

import edu.practice.dispatch.dto.NewAirMission;
import edu.practice.dispatch.error.ApiExceptionHandler;
import edu.practice.dispatch.error.MissionNotFoundException;
import edu.practice.dispatch.model.DispatchMission;
import edu.practice.dispatch.repo.MissionRepository;
import edu.practice.dispatch.service.DispatchService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static marking.Stubs.m;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P2 Task 3 – Validation, lookup and error handling [20 marks].
 * Error-mapping tests use a throw-only controller plus YOUR ApiExceptionHandler (no Task 4 endpoints needed).
 */
class Task3Test {

    @RestController
    static class Thrower {
        @GetMapping("/boom/missing")
        public String missing() { throw new MissionNotFoundException(4242); }

        @GetMapping("/boom/bad")
        public String bad() { throw new IllegalArgumentException("capacity must be positive"); }
    }

    static MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(new Thrower()).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    static NewAirMission body(long id, int distanceKm, int loadKg, int windPenalty, boolean critical) {
        return new NewAirMission(id, distanceKm, loadKg, windPenalty, critical);
    }

    // ---- 6 marks: energy(id), MissionNotFoundException for unknown IDs ----

    @Test
    void t3_energyReturnsMissionEnergy_m3() {
        DispatchService service = new DispatchService(new Stubs.StubRepo(m(5, 1, 77), m(6, 3, 12)));
        assertEquals(77, service.energy(5));
        assertEquals(12, service.energy(6));
    }

    @Test
    void t3_energyUnknownIdThrowsNotFound_m3() {
        DispatchService service = new DispatchService(new MissionRepository());
        assertThrows(MissionNotFoundException.class, () -> service.energy(999));
        assertThrows(MissionNotFoundException.class, () -> service.energy(0));
    }

    // ---- 7 marks: addAir validation (positive ID/distance, non-negative load/wind, unique ID, 0-10,000) ----

    @Test
    void t3_addAirCreatesAndSavesMission_m2() {
        MissionRepository repo = new MissionRepository();
        DispatchMission created = new DispatchService(repo).addAir(body(104, 6, 1, 2, false));
        assertEquals(104L, created.getId());
        assertEquals(6, created.getDistanceKm());
        assertEquals(1, created.getLoadKg());
        assertEquals(2, Reflect.call(created, "getWindPenalty"));
        assertSame(created, repo.find(104), "the new mission must be saved in the repository");
        DispatchMission edge = new DispatchService(repo).addAir(body(105, 10000, 0, 0, true));
        assertSame(edge, repo.find(105), "load 0, wind 0 and distance 10,000 are valid");
    }

    @Test
    void t3_addAirRejectsNonPositiveIdOrDistance_m2() {
        MissionRepository repo = new MissionRepository();
        DispatchService service = new DispatchService(repo);
        int before = repo.all().size();
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(0, 6, 1, 2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(-7, 6, 1, 2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(110, 0, 1, 2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(111, -3, 1, 2, false)));
        assertEquals(before, repo.all().size(), "rejected missions must not be stored");
    }

    @Test
    void t3_addAirRejectsNegativeLoadWindAndDuplicateId_m2() {
        MissionRepository repo = new MissionRepository();
        DispatchService service = new DispatchService(repo);
        DispatchMission original = repo.find(101);
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(120, 6, -1, 2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(121, 6, 1, -2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(101, 6, 1, 2, true)), "ID 101 already exists");
        assertSame(original, repo.find(101), "a duplicate must not overwrite mission 101");
        assertNull(repo.find(120));
        assertNull(repo.find(121));
    }

    @Test
    void t3_addAirRejectsValuesAbove10000_m1() {
        MissionRepository repo = new MissionRepository();
        DispatchService service = new DispatchService(repo);
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(130, 10001, 1, 2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(131, 6, 10001, 2, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(132, 6, 1, 10001, false)));
        assertThrows(IllegalArgumentException.class, () -> service.addAir(body(133, Integer.MAX_VALUE, 1, 2, false)));
        assertNull(repo.find(130));
        assertNull(repo.find(133));
    }

    // ---- 7 marks: reject non-positive capacity; 400 / 404 with ApiError BAD_REQUEST / NOT_FOUND ----

    @Test
    void t3_planRejectsNonPositiveCapacity_m2() {
        DispatchService service = new DispatchService(new Stubs.StubRepo(m(1, 3, 5)));
        assertThrows(IllegalArgumentException.class, () -> service.plan(0, false));
        assertThrows(IllegalArgumentException.class, () -> service.plan(-5, true));
    }

    @Test
    void t3_illegalArgumentGives400BadRequest_m2() throws Exception {
        mvc().perform(get("/boom/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void t3_missionNotFoundGives404NotFound_m2() throws Exception {
        mvc().perform(get("/boom/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void t3_errorBodiesCarryMessage_m1() throws Exception {
        mvc().perform(get("/boom/bad")).andExpect(jsonPath("$.message").isNotEmpty());
        mvc().perform(get("/boom/missing")).andExpect(jsonPath("$.message").isNotEmpty());
    }
}
