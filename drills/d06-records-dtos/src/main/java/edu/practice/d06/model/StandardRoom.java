package edu.practice.d06.model;

/** Given. A standard room: the same price every night. */
public class StandardRoom extends Room {

    private final int nightlyCents;

    public StandardRoom(long id, String number, int capacity, int nightlyCents) {
        super(id, number, capacity);
        if (nightlyCents <= 0) throw new IllegalArgumentException("nightlyCents must be positive");
        this.nightlyCents = nightlyCents;
    }

    public int getNightlyCents() { return nightlyCents; }

    @Override
    public String type() { return "STANDARD"; }

    /** nights x nightlyCents. Example: 2 nights at 9,000 = 18,000. */
    @Override
    public int priceCents(int nights) {
        if (nights <= 0) throw new IllegalArgumentException("nights must be positive");
        try {
            return Math.multiplyExact(nights, nightlyCents);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("price too large", e);
        }
    }
}
