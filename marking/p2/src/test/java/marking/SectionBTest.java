package marking;

import edu.practice.dispatch.DispatchApplication;
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

/** P2 Section B – six functional checks against the running app [20 marks]. */
@SpringBootTest(classes = DispatchApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SectionBTest {

    @Autowired
    TestRestTemplate rest;

    // 3 – GroundMission(101) consumes 39 energy units and has priority 3.
    @Test
    void b1_ground101Energy39Priority3_m3() {
        assertEquals(39, energy(101));
        assertEquals(18, energy(102), "Ground 102: 2x4 + 5x2");
        Map<Long, Map<String, Object>> lines = linesById(plan("capacity=1000&urgentOnly=false"));
        assertEquals(3, num(lines.get(101L).get("priority")));
        assertEquals(39, num(lines.get(101L).get("energyUnits")));
        assertEquals(1, num(lines.get(102L).get("priority")), "routine ground mission 102 has priority 1");
    }

    // 3 – AirMission(103) consumes 38 units and has priority 5.
    @Test
    void b2_air103Energy38Priority5_m3() {
        assertEquals(38, energy(103));
        Map<Long, Map<String, Object>> lines = linesById(plan("capacity=1000&urgentOnly=false"));
        assertEquals(5, num(lines.get(103L).get("priority")));
        assertEquals(38, num(lines.get(103L).get("energyUnits")));
    }

    // 3 – At capacity 60, greedy dispatch selects IDs [103,102] and skips [101].
    @Test
    void b3_greedyCapacity60Selects103And102_m3() {
        Map<String, Object> p = plan("capacity=60&urgentOnly=false");
        assertEquals(List.of(103L, 102L), selectedIds(p));
        assertEquals(List.of(101L), skipped(p));
        assertEquals(56, num(p.get("energyUsed")));
        assertEquals(4, num(p.get("energyRemaining")));
        // capacity 40: take 103 (2 left), skip 101 and 102
        Map<String, Object> p40 = plan("capacity=40&urgentOnly=false");
        assertEquals(List.of(103L), selectedIds(p40));
        assertEquals(List.of(101L, 102L), skipped(p40));
        assertEquals(2, num(p40.get("energyRemaining")));
    }

    // 3 – With urgentOnly=true, the routine mission is excluded from planning.
    @Test
    void b4_urgentOnlyExcludesRoutineMission_m3() {
        Map<String, Object> p = plan("capacity=60&urgentOnly=true");
        assertEquals(List.of(103L), selectedIds(p));
        assertEquals(List.of(101L), skipped(p));
        assertEquals(38, num(p.get("energyUsed")));
        assertEquals(22, num(p.get("energyRemaining")));
        Map<String, Object> big = plan("capacity=100&urgentOnly=true");
        assertEquals(List.of(103L, 101L), selectedIds(big), "102 (priority 1) is never planned");
        assertEquals(List.of(), skipped(big));
        assertEquals(77, num(big.get("energyUsed")));
    }

    // 4 – Bad capacity and invalid POST input return 400; an unknown ID returns 404.
    @Test
    void b5_badCapacityAndInvalidPostGive400_m2() {
        for (String q : new String[]{"capacity=0&urgentOnly=false", "capacity=-5&urgentOnly=true"}) {
            ResponseEntity<Map> r = rest.getForEntity("/api/missions/plan?" + q, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), q);
            assertEquals("BAD_REQUEST", r.getBody().get("code"));
        }
        List<Map<String, Object>> bad = List.of(
                Map.of("id", 150, "distanceKm", 0, "loadKg", 1, "windPenalty", 2, "critical", false),
                Map.of("id", 151, "distanceKm", 5, "loadKg", -1, "windPenalty", 2, "critical", false),
                Map.of("id", 152, "distanceKm", 5, "loadKg", 1, "windPenalty", 10001, "critical", false),
                Map.of("id", 101, "distanceKm", 5, "loadKg", 1, "windPenalty", 2, "critical", true));
        for (Map<String, Object> body : bad) {
            ResponseEntity<Map> r = rest.postForEntity("/api/missions/air", body, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), "POST " + body);
            assertEquals("BAD_REQUEST", r.getBody().get("code"));
        }
        List<Map<String, Object>> all = rest.getForObject("/api/missions", List.class);
        assertEquals(3, all.size(), "invalid missions must not be stored");
    }

    @Test
    void b5_unknownIdGives404_m2() {
        for (long id : new long[]{999, 7}) {
            ResponseEntity<Map> r = rest.getForEntity("/api/missions/" + id + "/energy", Map.class);
            assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
            assertEquals("NOT_FOUND", r.getBody().get("code"));
        }
    }

    // 4 – Successful POST returns 201, and subsequent planning incorporates the new mission.
    @Test
    void b6_validPostReturns201_m2() {
        ResponseEntity<Map> r = rest.postForEntity("/api/missions/air",
                Map.of("id", 104, "distanceKm", 6, "loadKg", 1, "windPenalty", 2, "critical", false), Map.class);
        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertEquals(104, num(r.getBody().get("id")));
    }

    @Test
    void b6_planIncorporatesNewMission_m2() {
        rest.postForEntity("/api/missions/air",
                Map.of("id", 104, "distanceKm", 6, "loadKg", 1, "windPenalty", 2, "critical", false), Map.class);
        assertEquals(30, energy(104), "Air 104: 3x6 + 8x1 + 2x2");
        // sorted [103(p5,38), 101(p3,39), 104(p2,30), 102(p1,18)]; capacity 70: take 103 (32 left), skip 101, take 104 (2 left), skip 102
        Map<String, Object> p = plan("capacity=70&urgentOnly=false");
        assertEquals(List.of(103L, 104L), selectedIds(p));
        assertEquals(List.of(101L, 102L), skipped(p));
        assertEquals(68, num(p.get("energyUsed")));
    }

    long energy(long id) {
        ResponseEntity<Map> r = rest.getForEntity("/api/missions/" + id + "/energy", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "GET /api/missions/" + id + "/energy");
        return num(r.getBody().get("energyUnits"));
    }

    Map<String, Object> plan(String query) {
        ResponseEntity<Map> r = rest.getForEntity("/api/missions/plan?" + query, Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "GET /api/missions/plan?" + query);
        return r.getBody();
    }

    static List<Long> selectedIds(Map<String, Object> plan) {
        return ((List<Map<String, Object>>) plan.get("selected")).stream().map(s -> num(s.get("id"))).toList();
    }

    static Map<Long, Map<String, Object>> linesById(Map<String, Object> plan) {
        Map<Long, Map<String, Object>> out = new java.util.HashMap<>();
        for (Map<String, Object> s : (List<Map<String, Object>>) plan.get("selected")) out.put(num(s.get("id")), s);
        return out;
    }

    static List<Long> skipped(Map<String, Object> plan) {
        return ((List<?>) plan.get("skipped")).stream().map(SectionBTest::num).toList();
    }

    static long num(Object o) {
        assertNotNull(o, "missing JSON field");
        return ((Number) o).longValue();
    }
}
