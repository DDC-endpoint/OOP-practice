package edu.practice.parking.model;

/** Charged per commenced hour: 1-60 minutes = 1 hour, 61-120 = 2 hours, ... */
public class HourlyPass extends ParkingPass {
    private final int rateCents;

    public HourlyPass(long id, String registration, int rateCents) {
        super(id, registration);
        this.rateCents = rateCents;
    }

    public int getRateCents() {
        return rateCents;
    }

    @Override
    public int priceCents(int stayMinutes) {
        if (stayMinutes <= 0) {
            throw new IllegalArgumentException("stayMinutes must be positive");
        }
        // Ceiling division done in long: (stayMinutes + 59) would overflow an int near Integer.MAX_VALUE.
        long hours = (stayMinutes + 59L) / 60;
        // Math.toIntExact throws ArithmeticException instead of silently wrapping.
        return Math.toIntExact(hours * rateCents);
    }
}
