package marking;

import edu.practice.dispatch.DispatchApplication;
import edu.practice.dispatch.dto.DispatchPlan;
import edu.practice.dispatch.dto.PlanLine;
import edu.practice.dispatch.service.DispatchService;
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

/**
 * P2 Task 4 – Expose three REST operations [20 marks]. Runs the real app on a random port.
 * Expected values come from the app's own DispatchService, so these marks are about the endpoints
 * (mapping, params, status, JSON) rather than your Task 1-2 formulas.
 */
@SpringBootTest(classes = DispatchApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Task4Test {

    static final Map<String, Object> AIR_104 =
            Map.of("id", 104, "distanceKm", 6, "loadKg", 1, "windPenalty", 2, "critical", false);

    @Autowired
    TestRestTemplate rest;

    @Autowired
    DispatchService service;

    // ---- 5 marks: GET /api/missions/{id}/energy returns a JSON object containing energyUnits ----

    @Test
    void t4_energyEndpointReturnsEnergyUnitsJson_m3() {
        ResponseEntity<Map> r = rest.getForEntity("/api/missions/101/energy", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertNotNull(r.getBody(), "the body must be a JSON object");
        assertTrue(r.getBody().containsKey("energyUnits"), "JSON object must contain energyUnits, got " + r.getBody());
        assertEquals(service.energy(101), num(r.getBody().get("energyUnits")));
    }

    @Test
    void t4_energyEndpointUsesPathId_m2() {
        ResponseEntity<Map> r = rest.getForEntity("/api/missions/102/energy", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertEquals(service.energy(102), num(r.getBody().get("energyUnits")));
        assertNotEquals(HttpStatus.OK, rest.getForEntity("/api/missions/999/energy", Map.class).getStatusCode(),
                "mission 999 does not exist");
    }

    // ---- 5 marks: POST /api/missions/air accepts NewAirMission JSON, returns 201 with the created mission ----

    @Test
    void t4_postAirReturns201_m3() {
        assertEquals(HttpStatus.CREATED, rest.postForEntity("/api/missions/air", AIR_104, Map.class).getStatusCode());
        Map<String, Object> other = Map.of("id", 222, "distanceKm", 30, "loadKg", 4, "windPenalty", 0, "critical", true);
        assertEquals(HttpStatus.CREATED, rest.postForEntity("/api/missions/air", other, Map.class).getStatusCode());
    }

    @Test
    void t4_postAirReturnsCreatedMission_m2() {
        ResponseEntity<Map> r = rest.postForEntity("/api/missions/air", AIR_104, Map.class);
        assertTrue(r.getStatusCode().is2xxSuccessful(), "POST should succeed, got " + r.getStatusCode());
        Map<?, ?> m = r.getBody();
        assertEquals(104, num(m.get("id")));
        assertEquals(6, num(m.get("distanceKm")));
        assertEquals(1, num(m.get("loadKg")));
        assertEquals(2, num(m.get("windPenalty")));
        List<Map<String, Object>> all = rest.getForObject("/api/missions", List.class);
        assertTrue(all.stream().anyMatch(x -> num(x.get("id")) == 104), "the mission must be stored");
    }

    // ---- 10 marks: GET /api/missions/plan?capacity=60&urgentOnly=false; urgentOnly defaults to false ----

    @Test
    void t4_planEndpointReturnsDispatchPlanJson_m4() {
        ResponseEntity<Map> r = rest.getForEntity("/api/missions/plan?capacity=60&urgentOnly=false", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        for (String key : new String[]{"capacity", "energyUsed", "energyRemaining", "selected", "skipped"}) {
            assertTrue(r.getBody().containsKey(key), "plan JSON must contain " + key);
        }
        assertSamePlan(service.plan(60, false), r.getBody());
    }

    @Test
    void t4_planEndpointPassesCapacityAndUrgentOnly_m3() {
        assertSamePlan(service.plan(45, true), rest.getForObject("/api/missions/plan?capacity=45&urgentOnly=true", Map.class));
        assertSamePlan(service.plan(100, false), rest.getForObject("/api/missions/plan?capacity=100&urgentOnly=false", Map.class));
        assertSamePlan(service.plan(40, false), rest.getForObject("/api/missions/plan?capacity=40&urgentOnly=false", Map.class));
    }

    @Test
    void t4_planUrgentOnlyDefaultsToFalse_m3() {
        ResponseEntity<Map> r = rest.getForEntity("/api/missions/plan?capacity=60", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "urgentOnly must be optional (defaultValue = \"false\")");
        assertSamePlan(service.plan(60, false), r.getBody());
        assertSamePlan(service.plan(70, false), rest.getForObject("/api/missions/plan?capacity=70", Map.class));
    }

    static void assertSamePlan(DispatchPlan expected, Map<?, ?> json) {
        assertNotNull(json);
        assertEquals(expected.capacity(), num(json.get("capacity")));
        assertEquals(expected.energyUsed(), num(json.get("energyUsed")));
        assertEquals(expected.energyRemaining(), num(json.get("energyRemaining")));
        List<Map<String, Object>> selected = (List<Map<String, Object>>) json.get("selected");
        assertEquals(expected.selected().stream().map(PlanLine::id).toList(),
                selected.stream().map(s -> num(s.get("id"))).toList(), "selected IDs");
        List<?> skipped = (List<?>) json.get("skipped");
        assertEquals(expected.skipped(), skipped.stream().map(Task4Test::num).toList(), "skipped IDs");
    }

    static long num(Object o) {
        assertNotNull(o, "missing JSON field");
        return ((Number) o).longValue();
    }
}
