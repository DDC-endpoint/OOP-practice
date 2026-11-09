package marking;

import edu.practice.courier.model.Parcel;
import edu.practice.courier.repo.ParcelRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test doubles so the Task 2/3/4 marks don't depend on your Task 1 arithmetic.
 * FixedParcel is a third kind of parcel with a price and priority you choose: code that only
 * works for StandardParcel/ExpressParcel by type-checking will fail on it.
 */
final class Stubs {

    private Stubs() { }

    public static final class FixedParcel extends Parcel {
        private final int price;
        private final int prio;

        FixedParcel(long id, String trackingCode, int weightGrams, int price, int prio) {
            super(id, trackingCode, weightGrams);
            this.price = price;
            this.prio = prio;
        }

        @Override public int pricePence() { return price; }
        @Override public int priority() { return prio; }
    }

    /** A repository holding exactly the parcels you pass, iterated in insertion order (no seed). */
    static final class StubRepo extends ParcelRepository {
        private final Map<Long, Parcel> map = new LinkedHashMap<>();

        StubRepo(Parcel... parcels) {
            for (Parcel p : parcels) map.put(p.getId(), p);
        }

        @Override public Collection<Parcel> all() { return List.copyOf(map.values()); }
        @Override public Parcel find(long id) { return map.get(id); }
        @Override public boolean exists(long id) { return map.containsKey(id); }
        @Override public void save(Parcel parcel) { map.put(parcel.getId(), parcel); }
    }
}
