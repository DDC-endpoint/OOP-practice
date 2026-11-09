package edu.practice.d05;

import edu.practice.d05.dto.Standing;
import edu.practice.d05.model.Team;
import edu.practice.d05.service.LeaderboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'LeaderboardServiceTest'.
 *
 * team(name, won, drawn, lost, goalsFor, goalsAgainst): points = 3 x won + drawn, GD = goalsFor - goalsAgainst.
 */
class LeaderboardServiceTest {

    private final LeaderboardService service = new LeaderboardService();

    private static Team team(String name, int won, int drawn, int lost, int goalsFor, int goalsAgainst) {
        return new Team(name, won, drawn, lost, goalsFor, goalsAgainst);
    }

    /** A whole league, deliberately in a scrambled order.                       points  GD */
    private static List<Team> league() {
        return new ArrayList<>(List.of(
                team("Town",      2, 1, 5,  8, 13),   //  7  -5
                team("Rovers",    5, 2, 1, 14,  6),   // 17  +8
                team("Albion",    2, 1, 5,  7, 15),   //  7  -8
                team("City",      4, 3, 1, 10,  8),   // 15  +2
                team("Rangers",   2, 0, 6,  5, 17),   //  6 -12
                team("Athletic",  5, 2, 1, 11,  3),   // 17  +8
                team("Wanderers", 4, 3, 1,  9,  9),   // 15   0
                team("United",    5, 2, 1, 12,  6))); // 17  +6
    }

    private static List<String> names(List<Team> teams) {
        List<String> names = new ArrayList<>();
        for (Team t : teams) names.add(t.getName());
        return names;
    }

    /** `first` must come before `second`, whichever way round they are compared. */
    private void assertBefore(Team first, Team second) {
        Comparator<Team> order = service.leaderboardOrder();
        assertNotNull(order, "leaderboardOrder() must return a Comparator");
        assertTrue(order.compare(first, second) < 0, first + " should come before " + second);
        assertTrue(order.compare(second, first) > 0, second + " should come after " + first);
    }

    // ---------------------------------------------------------------- the comparator, one rule at a time

    @Test
    @DisplayName("1. more points first, even with a worse goal difference or a later name")
    void morePointsFirst() {
        // Zeals: 9 pts, GD -3.  Ashford: 7 pts, GD +12. Points win.
        assertBefore(team("Zeals", 3, 0, 2, 6, 9), team("Ashford", 2, 1, 0, 15, 3));
        // Mills: 1 pt, GD -8.  Nash: 0 pts, GD 0.
        assertBefore(team("Mills", 0, 1, 2, 1, 9), team("Nash", 0, 0, 3, 0, 0));
    }

    @Test
    @DisplayName("2. equal points -> bigger goal difference first (also when both are negative)")
    void goalDifferenceBreaksPointsTie() {
        // both 8 pts: York +6, Ashby +1. GD wins, even though Ashby is first alphabetically.
        assertBefore(team("York", 2, 2, 1, 10, 4), team("Ashby", 2, 2, 1, 5, 4));
        // both 4 pts: Crewe -2 is better than Bury -5.
        assertBefore(team("Crewe", 1, 1, 3, 4, 6), team("Bury", 1, 1, 3, 3, 8));
    }

    @Test
    @DisplayName("3. equal points and goal difference -> name A to Z")
    void nameBreaksFullTie() {
        assertBefore(team("Barnet", 3, 1, 1, 8, 5), team("Bolton", 3, 1, 1, 8, 5));
        assertBefore(team("Walsall", 0, 2, 3, 2, 9), team("Wigan", 0, 2, 3, 2, 9));
        Team same = team("Exeter", 1, 1, 1, 3, 3);
        assertEquals(0, service.leaderboardOrder().compare(same, same), "a team compared with itself is a tie (0)");
    }

    // ---------------------------------------------------------------- sorted()

    @Test
    @DisplayName("4. sorted() puts a whole league in leaderboard order")
    void sortsWholeLeague() {
        assertEquals(List.of("Athletic", "Rovers", "United", "City", "Wanderers", "Town", "Albion", "Rangers"),
                names(service.sorted(league())));
        assertEquals(List.of("Crewe", "Bury"),
                names(service.sorted(List.of(team("Bury", 1, 1, 3, 3, 8), team("Crewe", 1, 1, 3, 4, 6)))));
    }

    @Test
    @DisplayName("5. sorted() returns a NEW list and never changes the one it was given")
    void sortedDoesNotTouchSource() {
        List<Team> source = league();
        List<String> before = names(source);
        List<Team> result = service.sorted(source);
        assertNotSame(source, result, "return a new list, don't sort the caller's list in place");
        assertEquals(before, names(source), "the original list must keep its original order");

        // List.of(...) can't be changed at all: calling .sort() on it throws UnsupportedOperationException.
        List<Team> fixed = List.of(team("Hull", 0, 0, 1, 0, 2), team("Bath", 1, 0, 0, 2, 0));
        assertEquals(List.of("Bath", "Hull"), names(assertDoesNotThrow(() -> service.sorted(fixed),
                "sorted() must also work on an unmodifiable list such as List.of(...)")));
    }

    // ---------------------------------------------------------------- table()

    @Test
    @DisplayName("6. table(min) keeps teams with AT LEAST min points, sorted, as Standing records")
    void tableFiltersSortsAndMaps() {
        List<Team> source = league();
        List<String> before = names(source);
        assertEquals(List.of(
                new Standing("Athletic", 17, 8),
                new Standing("Rovers", 17, 8),
                new Standing("United", 17, 6),
                new Standing("City", 15, 2),
                new Standing("Wanderers", 15, 0),
                new Standing("Town", 7, -5),
                new Standing("Albion", 7, -8)   // exactly 7 points: kept. Rangers (6) is left out.
        ), service.table(source, 7));
        assertEquals(List.of(
                new Standing("Athletic", 17, 8),
                new Standing("Rovers", 17, 8),
                new Standing("United", 17, 6)
        ), service.table(source, 16));
        assertEquals(before, names(source), "table() must not change the list it was given");
    }

    @Test
    @DisplayName("7. table(0) keeps everyone; a minimum nobody reaches gives an empty list")
    void tableEdges() {
        List<Standing> all = service.table(league(), 0);
        assertEquals(8, all.size());
        assertEquals(new Standing("Athletic", 17, 8), all.get(0));
        assertEquals(new Standing("Rangers", 6, -12), all.get(7));
        assertEquals(List.of(), service.table(league(), 18));
    }

    @Test
    @DisplayName("8. table rejects a negative minimum")
    void tableRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> service.table(league(), -1));
        assertThrows(IllegalArgumentException.class, () -> service.table(league(), -50));
    }
}
