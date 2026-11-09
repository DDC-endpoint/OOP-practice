package edu.practice.dispatch.model;

public class GroundMission extends DispatchMission {
    private final boolean medical;

    public GroundMission(long id, int distanceKm, int loadKg, boolean medical) {
        super(id, distanceKm, loadKg);
        this.medical = medical;
    }

    public boolean isMedical() {
        return medical;
    }

    /** 2 x distanceKm + 5 x loadKg, computed in long; Math.toIntExact throws if it does not fit an int. */
    @Override
    public int energyUnits() {
        long energy = 2L * getDistanceKm() + 5L * getLoadKg();
        return Math.toIntExact(energy);
    }

    @Override
    public int priority() {
        return medical ? 3 : 1;
    }
}
