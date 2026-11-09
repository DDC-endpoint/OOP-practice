package edu.practice.d10.repo;

import edu.practice.d10.model.Screening;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory storage, the same shape as the LBP starters' repositories. */
@Repository
public class ScreeningRepository {

    private final Map<Long, Screening> screenings = new ConcurrentHashMap<>();

    public ScreeningRepository() {
        screenings.put(1L, new Screening(1, "The Last Lighthouse", 95, 900));
        screenings.put(2L, new Screening(2, "Orbit of Ash", 150, 1_000));
        screenings.put(3L, new Screening(3, "Paper Kingdoms", 128, 950));
        screenings.put(4L, new Screening(4, "Small Hours", 104, 1_100));
        screenings.put(5L, new Screening(5, "Midnight Ferry", 200, 1_000));
        screenings.put(6L, new Screening(6, "Glass Harbour", 180, 400));
    }

    public Collection<Screening> all() { return List.copyOf(screenings.values()); }

    public Screening get(long id) { return screenings.get(id); }

    public boolean contains(long id) { return screenings.containsKey(id); }

    public void save(Screening screening) { screenings.put(screening.getId(), screening); }
}
