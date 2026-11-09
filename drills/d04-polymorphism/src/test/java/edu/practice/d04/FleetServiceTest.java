package edu.practice.d04;

import edu.practice.d04.dto.CostQuote;
import edu.practice.d04.model.*;
import edu.practice.d04.repo.VehicleRepository;
import edu.practice.d04.service.FleetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'FleetServiceTest'.
 *
 * The repository starts with:
 *   #1 VAN-01    Van        30 c per km
 *   #2 BIKE-02   CargoBike  80 c per started 25 km
 *   #3 TRUCK-03  Truck      1500 c base + 20 c per km
 *   #4 VAN-04    Van        22 c per km
 */
class FleetServiceTest {

    private final VehicleRepository repo = new VehicleRepository();
    private final FleetService service = new FleetService(repo);

    private List<Vehicle> wholeFleet() {
        return List.of(repo.get(1L), repo.get(2L), repo.get(3L), repo.get(4L));
    }

    private static List<Long> ids(List<CostQuote> quotes) {
        List<Long> ids = new ArrayList<>();
        for (CostQuote q : quotes) ids.add(q.vehicleId());
        return ids;
    }

    private static List<Integer> costs(List<CostQuote> quotes) {
        List<Integer> costs = new ArrayList<>();
        for (CostQuote q : quotes) costs.add(q.costCents());
        return costs;
    }

    // ---------------------------------------------------------------- convoyCostCents

    @Test
    @DisplayName("1. convoyCostCents adds up each vehicle's OWN cost rule")
    void convoyUsesEachVehiclesRule() {
        // 10 km: van 300 + bike 80 + truck 1700 + van 220
        assertEquals(2_300, service.convoyCostCents(wholeFleet(), 10));
        // 200 km: van 6000 + bike 640 (8 charges) + truck 5500 + van 4400
        assertEquals(16_540, service.convoyCostCents(wholeFleet(), 200));
    }

    @Test
    @DisplayName("2. convoyCostCents works for any List<Vehicle>, not just the repository's")
    void convoyWorksForAnyList() {
        List<Vehicle> convoy = List.of(new Truck(5, "T-5", 2_000, 10), new CargoBike(6, "B-6", 75));
        assertEquals(2_510 + 225, service.convoyCostCents(convoy, 51));     // bike: 51 km = 3 charges
        assertEquals(57, service.convoyCostCents(List.of(new Van(7, "V-7", 19)), 3));
        assertEquals(0, service.convoyCostCents(List.of(), 40), "an empty convoy costs nothing");
    }

    @Test
    @DisplayName("3. convoyCostCents rejects km <= 0 (even for an empty convoy) and an overflowing total")
    void convoyValidation() {
        assertThrows(IllegalArgumentException.class, () -> service.convoyCostCents(wholeFleet(), 0));
        assertThrows(IllegalArgumentException.class, () -> service.convoyCostCents(List.of(), -5));
        // each van alone costs 1,500,000,000 c (fits in an int); together they don't
        List<Vehicle> pricey = List.of(new Van(8, "V-8", 1_000_000), new Van(9, "V-9", 1_000_000));
        assertThrows(IllegalArgumentException.class, () -> service.convoyCostCents(pricey, 1_500),
                "use Math.addExact and turn ArithmeticException into IllegalArgumentException");
    }

    // ---------------------------------------------------------------- quotes

    @Test
    @DisplayName("4. quotes(10): one CostQuote per vehicle, every field filled in, cheapest first")
    void quotesAtTenKm() {
        assertEquals(List.of(
                new CostQuote(2, "BIKE-02", "CARGO_BIKE", 10, 80),
                new CostQuote(4, "VAN-04", "VAN", 10, 220),
                new CostQuote(1, "VAN-01", "VAN", 10, 300),
                new CostQuote(3, "TRUCK-03", "TRUCK", 10, 1_700)
        ), service.quotes(10));
    }

    @Test
    @DisplayName("5. the order depends on the distance (it really is sorted by cost)")
    void orderChangesWithDistance() {
        List<CostQuote> far = service.quotes(200);
        assertEquals(List.of(2L, 4L, 3L, 1L), ids(far), "at 200 km the truck beats van #1");
        assertEquals(List.of(640, 4_400, 5_500, 6_000), costs(far));
        assertEquals(200, far.get(0).km());

        List<CostQuote> near = service.quotes(1);
        assertEquals(List.of(4L, 1L, 2L, 3L), ids(near), "at 1 km both vans beat the bike");
        assertEquals(List.of(22, 30, 80, 1_520), costs(near));
    }

    @Test
    @DisplayName("6. equal cost -> lower vehicle id first")
    void tieBreakOnId() {
        repo.save(new Van(16, "VAN-16", 30));   // same rate as VAN-01, so the same cost at any distance
        List<CostQuote> quotes = service.quotes(10);
        assertEquals(List.of(2L, 4L, 1L, 16L, 3L), ids(quotes));
        assertEquals(List.of(80, 220, 300, 300, 1_700), costs(quotes));
        // 150 km: bike 480, van#4 3300, then THREE vehicles at 4500 (van#1, truck#3, van#16) -> by id
        assertEquals(List.of(2L, 4L, 1L, 3L, 16L), ids(service.quotes(150)));
        assertEquals(List.of(480, 3_300, 4_500, 4_500, 4_500), costs(service.quotes(150)));
    }

    @Test
    @DisplayName("7. quotes rejects km <= 0")
    void quotesValidation() {
        assertThrows(IllegalArgumentException.class, () -> service.quotes(0));
        assertThrows(IllegalArgumentException.class, () -> service.quotes(-12));
    }

    // ---------------------------------------------------------------- source rules

    @Test
    @DisplayName("8. FleetService calls costCents(...) and never uses instanceof, getClass(), casts or subclass names")
    void noTypeChecksInService() throws IOException {
        String code = withoutComments(source());
        assertTrue(Pattern.compile("\\.costCents\\s*\\(").matcher(code).find(),
                "FleetService should call costCents(...) on each Vehicle");
        assertFalse(Pattern.compile("\\binstanceof\\b").matcher(code).find(),
                "don't use instanceof: let each Vehicle answer costCents() itself");
        assertFalse(Pattern.compile("\\bgetClass\\s*\\(").matcher(code).find(),
                "don't use getClass(): kind() already tells you the type label");
        Matcher named = Pattern.compile("\\b(Van|Truck|CargoBike)\\b").matcher(code);
        if (named.find()) {
            fail("FleetService mentions the subclass " + named.group()
                    + ": work only with the Vehicle type (no casts, no imports of subclasses)");
        }
    }

    /** Finds FleetService.java whether the tests run from the drill folder or the repo root. */
    private static String source() throws IOException {
        String file = "src/main/java/edu/practice/d04/service/FleetService.java";
        for (Path p : List.of(Path.of(file), Path.of("drills/d04-polymorphism", file))) {
            if (Files.exists(p)) return Files.readString(p);
        }
        fail("Could not find " + file + " (run the tests from the d04-polymorphism module)");
        return "";
    }

    /** Comments may mention instanceof (e.g. "don't use instanceof"); only real code is checked. */
    private static String withoutComments(String code) {
        return code.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("//[^\\n]*", " ");
    }
}
