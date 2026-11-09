package marking;

import edu.practice.dispatch.model.DispatchMission;
import edu.practice.dispatch.repo.MissionRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test doubles so Task 2/3 service marks don't depend on your Task 1/2 energy formulas.
 * FixedMission is a third kind of mission with a fixed energy and priority: a planner that
 * type-checks GroundMission/AirMission instead of calling the overridden methods fails on it.
 */
final class Stubs {

    private Stubs() { }

    static final class FixedMission extends DispatchMission {
        private final int energy;
        private final int priority;

        FixedMission(long id, int priority, int energy) {
            super(id, 1, 1);
            this.priority = priority;
            this.energy = energy;
        }

        @Override public int energyUnits() { return energy; }
        @Override public int priority() { return priority; }
    }

    /** A repository with exactly the missions you put in, iterated in insertion order (no seed). */
    static final class StubRepo extends MissionRepository {
        private final Map<Long, DispatchMission> map = new LinkedHashMap<>();

        StubRepo(DispatchMission... missions) {
            for (DispatchMission m : missions) map.put(m.getId(), m);
        }

        @Override public Collection<DispatchMission> all() { return List.copyOf(map.values()); }
        @Override public DispatchMission find(long id) { return map.get(id); }
        @Override public boolean exists(long id) { return map.containsKey(id); }
        @Override public void save(DispatchMission m) { map.put(m.getId(), m); }
    }

    static FixedMission m(long id, int priority, int energy) {
        return new FixedMission(id, priority, energy);
    }
}
