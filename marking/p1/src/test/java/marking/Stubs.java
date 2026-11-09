package marking;

import edu.practice.parking.model.ParkingPass;
import edu.practice.parking.repo.PassRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test doubles so Task 2/3 service marks don't depend on your Task 1 arithmetic.
 * FixedPass is a third kind of pass (price = minutes x centsPerMinute): code that only works
 * for HourlyPass/WeekendPass by type-checking will fail on it.
 */
final class Stubs {

    private Stubs() { }

    static final class FixedPass extends ParkingPass {
        private final int centsPerMinute;

        FixedPass(long id, String registration, int centsPerMinute) {
            super(id, registration);
            this.centsPerMinute = centsPerMinute;
        }

        @Override
        public int priceCents(int stayMinutes) {
            if (stayMinutes <= 0) throw new IllegalArgumentException("stayMinutes must be positive");
            return stayMinutes * centsPerMinute;
        }
    }

    /** A repository with exactly the passes you put in, iterated in insertion order (no seed). */
    static final class StubRepo extends PassRepository {
        private final Map<Long, ParkingPass> map = new LinkedHashMap<>();

        StubRepo(ParkingPass... passes) {
            for (ParkingPass p : passes) map.put(p.getId(), p);
        }

        @Override public Collection<ParkingPass> all() { return List.copyOf(map.values()); }
        @Override public ParkingPass get(long id) { return map.get(id); }
        @Override public boolean contains(long id) { return map.containsKey(id); }
        @Override public void save(ParkingPass pass) { map.put(pass.getId(), pass); }
    }
}
