package edu.practice.d04.service;

import edu.practice.d04.dto.CostQuote;
import edu.practice.d04.model.Vehicle;
import edu.practice.d04.repo.VehicleRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic for the delivery fleet.
 *
 * RULE FOR THIS WHOLE FILE: only ever talk to vehicles as `Vehicle`.
 * No type checks, no casts, no mentioning the subclasses by name.
 * Let each object run its own costCents(...) rule (dynamic dispatch).
 */
public class FleetService {

    private final VehicleRepository repo;

    public FleetService(VehicleRepository repo) {
        this.repo = repo;
    }

    /**
     * Total cost of sending EVERY vehicle in `convoy` the same distance `km`.
     * An empty convoy costs 0. km <= 0 or a total too big for an int -> IllegalArgumentException.
     */
    public int convoyCostCents(List<Vehicle> convoy, int km) {
        if (km <= 0) throw new IllegalArgumentException("km must be positive");
        int total = 0;
        for (Vehicle v : convoy) {              // v's TYPE is Vehicle; the OBJECT may be a Van, a Truck...
            try {
                total = Math.addExact(total, v.costCents(km));   // ...and Java runs that object's own rule
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("total cost too large", e);
            }
        }
        return total;
    }

    /**
     * One CostQuote for every vehicle in the repository, for a trip of `km` kilometres,
     * sorted cheapest first; equal costs -> lower vehicle id first. km <= 0 -> IllegalArgumentException.
     */
    public List<CostQuote> quotes(int km) {
        if (km <= 0) throw new IllegalArgumentException("km must be positive");
        return repo.all().stream()
                .map(v -> toQuote(v, km))
                .sorted(Comparator.comparingInt(CostQuote::costCents)
                        .thenComparingLong(CostQuote::vehicleId))
                .toList();
    }

    private CostQuote toQuote(Vehicle v, int km) {
        return new CostQuote(v.getId(), v.getPlate(), v.kind(), km, v.costCents(km));
    }
}
