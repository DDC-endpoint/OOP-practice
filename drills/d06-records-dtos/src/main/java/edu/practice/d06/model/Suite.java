package edu.practice.d06.model;

/** Given. A suite: a nightly rate plus a one-off service fee per stay (welcome hamper, butler...). */
public class Suite extends Room {

    private final int nightlyCents;
    private final int serviceFeeCents;

    public Suite(long id, String number, int capacity, int nightlyCents, int serviceFeeCents) {
        super(id, number, capacity);
        if (nightlyCents <= 0) throw new IllegalArgumentException("nightlyCents must be positive");
        if (serviceFeeCents < 0) throw new IllegalArgumentException("serviceFeeCents cannot be negative");
        this.nightlyCents = nightlyCents;
        this.serviceFeeCents = serviceFeeCents;
    }

    public int getNightlyCents() { return nightlyCents; }

    public int getServiceFeeCents() { return serviceFeeCents; }

    @Override
    public String type() { return "SUITE"; }

    /** nights x nightlyCents + serviceFeeCents (once). Example: 1 night at 11,000 + 4,000 = 15,000. */
    @Override
    public int priceCents(int nights) {
        if (nights <= 0) throw new IllegalArgumentException("nights must be positive");
        try {
            return Math.addExact(Math.multiplyExact(nights, nightlyCents), serviceFeeCents);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("price too large", e);
        }
    }
}
