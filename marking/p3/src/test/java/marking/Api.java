package marking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Small HTTP/JSON helpers for the P3 Spring tests. */
final class Api {

    private Api() { }

    static Map<String, Object> plan(int nowTick, int horizon, int cpu, int mem) {
        return Map.of("nowTick", nowTick, "horizon", horizon, "cpuCapacity", cpu, "memoryCapacity", mem);
    }

    static Map<String, Object> commit(String runKey, int nowTick, int horizon, int cpu, int mem) {
        return Map.of("runKey", runKey, "nowTick", nowTick, "horizon", horizon, "cpuCapacity", cpu, "memoryCapacity", mem);
    }

    static final Map<String, Object> ILLUSTRATIVE = plan(0, 6, 6, 10000);

    /** POST and expect 2xx; returns the JSON body. */
    static Map<String, Object> postOk(TestRestTemplate rest, String url, Object body) {
        ResponseEntity<Map> r = rest.postForEntity(url, body, Map.class);
        assertTrue(r.getStatusCode().is2xxSuccessful(), "POST " + url + " " + body + " -> " + r.getStatusCode() + " " + r.getBody());
        return r.getBody();
    }

    /** Selected reservations as [id, start, endExclusive, cpu, mem] rows. */
    static List<List<Long>> selected(Map<String, Object> plan) {
        assertNotNull(plan.get("selected"), "plan JSON must contain selected");
        return ((List<Map<String, Object>>) plan.get("selected")).stream().map(Api::row).toList();
    }

    static List<List<Long>> rows(List<Map<String, Object>> reservations) {
        return reservations.stream().map(Api::row).toList();
    }

    static List<Long> row(Map<String, Object> r) {
        return List.of(num(r.get("id")), num(r.get("startTick")), num(r.get("endTickExclusive")),
                num(r.get("cpuCores")), num(r.get("memoryMb")));
    }

    /** Rejected as "id:REASON" strings. */
    static List<String> rejected(Map<String, Object> plan) {
        assertNotNull(plan.get("rejected"), "plan JSON must contain rejected");
        return ((List<Map<String, Object>>) plan.get("rejected")).stream()
                .map(r -> num(r.get("id")) + ":" + r.get("reason")).toList();
    }

    static List<Map<String, Object>> reservations(TestRestTemplate rest) {
        ResponseEntity<List> r = rest.getForEntity("/api/workloads/reservations", List.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "GET /api/workloads/reservations");
        return r.getBody();
    }

    static List<Long> rankedIds(TestRestTemplate rest, int nowTick) {
        ResponseEntity<List> r = rest.getForEntity("/api/workloads/ranked?nowTick=" + nowTick, List.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "GET /api/workloads/ranked?nowTick=" + nowTick);
        return ((List<Map<String, Object>>) r.getBody()).stream().map(m -> num(m.get("id"))).toList();
    }

    /**
     * GET /api/workloads/{id}/urgency?nowTick=... The paper doesn't fix the JSON shape, so this accepts
     * a bare number or an object with an "urgency" (or "urgencyAt" / "value") field.
     */
    static long urgency(TestRestTemplate rest, long id, int nowTick) {
        String url = "/api/workloads/" + id + "/urgency?nowTick=" + nowTick;
        ResponseEntity<String> r = rest.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "GET " + url);
        String body = r.getBody() == null ? "" : r.getBody().trim();
        if (body.matches("-?\\d+")) return Long.parseLong(body);
        try {
            Map<?, ?> m = new ObjectMapper().readValue(body, Map.class);
            for (String key : new String[]{"urgency", "urgencyAt", "value"}) {
                if (m.get(key) instanceof Number n) return n.longValue();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return fail("GET " + url + " should return the urgency as a number or {\"urgency\": n}, got " + body);
    }

    static long num(Object o) {
        assertNotNull(o, "missing JSON field");
        return ((Number) o).longValue();
    }
}
