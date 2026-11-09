package edu.practice.dispatch.model;

public class AirMission extends DispatchMission {
    private final int windPenalty;
    private final boolean critical;

    public AirMission(long id, int distanceKm, int loadKg, int windPenalty, boolean critical) {
        super(id, distanceKm, loadKg);
        this.windPenalty = windPenalty;
        this.critical = critical;
    }

    public int getWindPenalty() {
        return windPenalty;
    }

    public boolean isCritical() {
        return critical;
    }

    /** 3 x distanceKm + 8 x loadKg + 2 x windPenalty, computed in long and converted safely. */
    @Override
    public int energyUnits() {
        long energy = 3L * getDistanceKm() + 8L * getLoadKg() + 2L * windPenalty;
        return Math.toIntExact(energy);
    }

    @Override
    public int priority() {
        return critical ? 5 : 2;
    }
}
