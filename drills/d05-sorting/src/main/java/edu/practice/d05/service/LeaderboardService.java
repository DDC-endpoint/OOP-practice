package edu.practice.d05.service;

import edu.practice.d05.dto.Standing;
import edu.practice.d05.model.Team;

import java.util.Comparator;
import java.util.List;

/**
 * Builds the tournament leaderboard.
 *
 * Leaderboard order:
 *   1. more points first                         (points DESC)
 *   2. equal points -> bigger goal difference first (goal difference DESC)
 *   3. still equal  -> name alphabetically, A to Z  (name ASC)
 *
 * None of these methods may change the list they are given.
 */
public class LeaderboardService {

    /** The leaderboard order as ONE Comparator. */
    public Comparator<Team> leaderboardOrder() {
        return Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparing(Team::getGoalDifference, Comparator.reverseOrder())
                .thenComparing(Team::getName);
    }

    /** A NEW list with the teams in leaderboard order. `teams` itself is left untouched. */
    public List<Team> sorted(List<Team> teams) {
        return teams.stream()
                .sorted(leaderboardOrder())
                .toList();
    }

    /**
     * The published table: only teams with at least `minPoints` points, in leaderboard order,
     * each turned into a Standing. minPoints < 0 -> IllegalArgumentException.
     */
    public List<Standing> table(List<Team> teams, int minPoints) {
        if (minPoints < 0) throw new IllegalArgumentException("minPoints cannot be negative");
        return teams.stream()
                .filter(t -> t.getPoints() >= minPoints)
                .sorted(leaderboardOrder())
                .map(t -> new Standing(t.getName(), t.getPoints(), t.getGoalDifference()))
                .toList();
    }
}
