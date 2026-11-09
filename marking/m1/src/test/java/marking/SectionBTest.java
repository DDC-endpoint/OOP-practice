package marking;

import edu.practice.courier.CourierApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/** M1 Section B – six functional checks against the running app [20 marks]. */
@SpringBootTest(classes = CourierApplication.class, webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SectionBTest {

    @Autowired
    TestRestTemplate rest;

    // 3 – StandardParcel 1 (1,200 g at 180 p per block) quotes 540 pence with priority 1.
    @Test
    void b1_standardParcel1Quotes540WithPriority1_m3() {
        Map<String, Object> q = quote(1);
        assertEquals(540L, num(q, "pricePence"));
        assertEquals(1L, num(q, "priority"));
        assertEquals("CP-1001", q.get("trackingCode"));
        Map<String, Object> q2 = quote(2);
        assertEquals(250L, num(q2, "pricePence"), "parcel 2: exactly 500 g is one block x 250");
        assertEquals(1L, num(q2, "priority"));
    }

    // 3 – ExpressParcel 3 (900 g, base 400 p, signature) quotes 1,060 pence with priority 3.
    @Test
    void b2_expressParcel3Quotes1060WithPriority3_m3() {
        Map<String, Object> q = quote(3);
        assertEquals(1060L, num(q, "pricePence"));
        assertEquals(3L, num(q, "priority"));
        Map<String, Object> q4 = quote(4);
        assertEquals(530L, num(q4, "pricePence"), "parcel 4: 350 + 2 x 90, no signature");
        assertEquals(2L, num(q4, "priority"));
    }

    // 3 – At capacityGrams=2000 the van loads [3,4,2], skips [1], uses 1,700 g and totals 1,840 pence.
    @Test
    void b3_van2000LoadsThreeAndSkipsOne_m3() {
        Map<String, Object> v = van("/api/parcels/van?capacityGrams=2000");
        assertEquals(List.of(3L, 4L, 2L), loadedIds(v));
        assertEquals(List.of(1L), longs(v.get("skipped")));
        assertEquals(1700L, num(v, "weightUsed"));
        assertEquals(300L, num(v, "weightRemaining"));
        assertEquals(1840L, num(v, "totalPence"));
        Map<String, Object> small = van("/api/parcels/van?capacityGrams=600");
        assertEquals(List.of(4L), loadedIds(small), "900 g parcel 3 doesn't fit in 600 g, but parcel 4 (300 g) does");
        assertEquals(List.of(3L, 2L, 1L), longs(small.get("skipped")));
        assertEquals(530L, num(small, "totalPence"));
    }

    // 3 – Without capacityGrams the van uses 2000, and repeated calls return identical results.
    @Test
    void b4_vanDefaultsTo2000AndIsDeterministic_m3() {
        Map<String, Object> byDefault = van("/api/parcels/van");
        assertEquals(2000L, num(byDefault, "capacityGrams"));
        assertEquals(van("/api/parcels/van?capacityGrams=2000"), byDefault, "no parameter must equal capacityGrams=2000");
        assertEquals(byDefault, van("/api/parcels/van"), "repeat calls must match");
        Map<String, Object> other = van("/api/parcels/van?capacityGrams=1300");
        assertEquals(List.of(3L, 4L), loadedIds(other));
        assertEquals(List.of(2L, 1L), longs(other.get("skipped")));
        assertEquals(other, van("/api/parcels/van?capacityGrams=1300"));
    }

    // 4 – Unknown ID -> 404 NOT_FOUND; bad capacity or invalid POST -> 400 BAD_REQUEST; nothing invalid stored.
    @Test
    void b5_unknownIdGives404AndBadInputGives400_m2() {
        for (String url : new String[]{"/api/parcels/999/quote", "/api/parcels/77/quote"}) {
            ResponseEntity<Map> r = rest.getForEntity(url, Map.class);
            assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode(), url);
            assertEquals("NOT_FOUND", r.getBody().get("code"), url);
            assertNotNull(r.getBody().get("message"));
        }
        for (String url : new String[]{"/api/parcels/van?capacityGrams=0", "/api/parcels/van?capacityGrams=-5"}) {
            ResponseEntity<Map> r = rest.getForEntity(url, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), url);
            assertEquals("BAD_REQUEST", r.getBody().get("code"), url);
        }
        List<Map<String, Object>> bad = List.of(
                body(5, "   ", 500, 100),
                body(5, "CP-1005", 0, 100),
                body(5, "CP-1005", 30_001, 100),
                body(5, "CP-1005", 500, 0),
                body(0, "CP-1005", 500, 100));
        for (Map<String, Object> b : bad) {
            ResponseEntity<Map> r = rest.postForEntity("/api/parcels/standard", b, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode(), "POST " + b);
            assertEquals("BAD_REQUEST", r.getBody().get("code"), "POST " + b);
        }
        assertEquals(List.of(1L, 2L, 3L, 4L), listedIds(), "nothing invalid may be stored");
    }

    // 4 (cont.) – A duplicate ID or tracking code -> 409 CONFLICT, and the original parcel is kept.
    @Test
    void b5_duplicatePostGives409Conflict_m2() {
        for (Map<String, Object> b : List.of(body(1, "CP-7001", 500, 100), body(9, "cp-1003", 500, 100))) {
            ResponseEntity<Map> r = rest.postForEntity("/api/parcels/standard", b, Map.class);
            assertEquals(HttpStatus.CONFLICT, r.getStatusCode(), "POST " + b);
            assertEquals("CONFLICT", r.getBody().get("code"), "POST " + b);
            assertNotNull(r.getBody().get("message"));
        }
        assertEquals(List.of(1L, 2L, 3L, 4L), listedIds(), "a duplicate must not be stored");
        assertEquals("CP-1001", quote(1).get("trackingCode"), "the duplicate must not replace parcel 1");
    }

    // 4 – A valid POST returns 201 and the new parcel is quoted and loaded in later requests.
    @Test
    void b6_validPostReturns201_m2() {
        ResponseEntity<Map> r = rest.postForEntity("/api/parcels/standard", body(5, "CP-1005", 2600, 150), Map.class);
        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertEquals(5L, num(r.getBody(), "id"));
        assertEquals("CP-1005", r.getBody().get("trackingCode"));
    }

    @Test
    void b6_postedParcelIsQuotedAndLoadedLater_m2() {
        ResponseEntity<Map> r = rest.postForEntity("/api/parcels/standard", body(6, " CP-2006 ", 2600, 150), Map.class);
        assertTrue(r.getStatusCode().is2xxSuccessful(), "POST should succeed, got " + r.getStatusCode());
        assertTrue(listedIds().contains(6L), "parcel 6 must be listed");
        Map<String, Object> q = quote(6);
        assertEquals(900L, num(q, "pricePence"), "2,600 g = 6 blocks x 150");
        assertEquals(1L, num(q, "priority"));
        assertEquals("CP-2006", q.get("trackingCode"), "stored trimmed");
        Map<String, Object> v = van("/api/parcels/van?capacityGrams=6000");
        assertEquals(List.of(3L, 4L, 2L, 1L, 6L), loadedIds(v), "everything fits in 6,000 g; parcel 6 is the dearest priority-1 parcel");
        assertEquals(3280L, num(v, "totalPence"));
        assertEquals(500L, num(v, "weightRemaining"));
    }

    // ---- helpers ----

    Map<String, Object> quote(long id) {
        ResponseEntity<Map> r = rest.getForEntity("/api/parcels/" + id + "/quote", Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), "GET /api/parcels/" + id + "/quote");
        return r.getBody();
    }

    Map<String, Object> van(String url) {
        ResponseEntity<Map> r = rest.getForEntity(url, Map.class);
        assertEquals(HttpStatus.OK, r.getStatusCode(), url);
        return r.getBody();
    }

    List<Long> listedIds() {
        List<Map<String, Object>> parcels = rest.getForObject("/api/parcels", List.class);
        return parcels.stream().map(p -> num(p, "id")).sorted().toList();
    }

    static Map<String, Object> body(long id, String trackingCode, int weightGrams, int ratePerBlockPence) {
        Map<String, Object> b = new HashMap<>();
        b.put("id", id);
        b.put("trackingCode", trackingCode);
        b.put("weightGrams", weightGrams);
        b.put("ratePerBlockPence", ratePerBlockPence);
        return b;
    }

    @SuppressWarnings("unchecked")
    static List<Long> loadedIds(Map<String, Object> van) {
        return ((List<Map<String, Object>>) van.get("loaded")).stream().map(q -> num(q, "id")).toList();
    }

    @SuppressWarnings("unchecked")
    static List<Long> longs(Object list) {
        return ((List<Object>) list).stream().map(n -> ((Number) n).longValue()).toList();
    }

    static long num(Map<?, ?> map, String key) {
        Object v = map.get(key);
        assertNotNull(v, "missing JSON field '" + key + "'");
        return ((Number) v).longValue();
    }
}
