package edu.practice.d09.repo;

import edu.practice.d09.model.Plant;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory storage, the same shape as the LBP starters' repositories. Given. */
@Repository
public class PlantRepository {

    private final Map<Long, Plant> plants = new ConcurrentHashMap<>();

    public PlantRepository() {
        plants.put(1L, new Plant(1, "Lavender", 450, 12));
        plants.put(2L, new Plant(2, "Olive tree", 3_999, 3));
        plants.put(3L, new Plant(3, "Basil", 250, 0));       // sold out
        plants.put(4L, new Plant(4, "Fern", 1_200, 5));
        plants.put(5L, new Plant(5, "Mint", 300, 8));
    }

    public Collection<Plant> all() { return List.copyOf(plants.values()); }

    public Plant get(long id) { return plants.get(id); }

    public boolean contains(long id) { return plants.containsKey(id); }

    public void save(Plant plant) { plants.put(plant.getId(), plant); }
}
