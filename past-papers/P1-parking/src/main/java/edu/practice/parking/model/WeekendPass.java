package edu.practice.parking.model;

/** Flat price for up to 480 minutes, then 150 cents for each commenced extra hour. */
public class WeekendPass extends ParkingPass {
    private static final int INCLUDED_MINUTES = 480;
    private static final int EXTRA_HOUR_CENTS = 150;

    private final int flatCents;

    public WeekendPass(long id, String registration, int flatCents) {
        super(id, registration);
        this.flatCents = flatCents;
    }

    public int getFlatCents() {
        return flatCents;
    }

    @Override
    public int priceCents(int stayMinutes) {
        if (stayMinutes <= 0) {
            throw new IllegalArgumentException("stayMinutes must be positive");
        }
        if (stayMinutes <= INCLUDED_MINUTES) {
            return flatCents;
        }
        long extraHours = (stayMinutes - INCLUDED_MINUTES + 59L) / 60;   // commenced hours, rounded up
        return Math.toIntExact(flatCents + extraHours * EXTRA_HOUR_CENTS);
    }
}
