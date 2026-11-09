package edu.practice.d10.model;

/**
 * One screening of a film. Jackson turns the getters into JSON:
 * {"id":1,"film":"The Last Lighthouse","runtimeMinutes":95,"basePriceCents":900}.
 */
public class Screening {

    /** The base price covers a film of up to 120 minutes. */
    public static final int INCLUDED_MINUTES = 120;
    /** Beyond that, every started 30 minutes is one surcharge block... */
    public static final int BLOCK_MINUTES = 30;
    /** ...and each block adds 150 cents to the seat price. */
    public static final int SURCHARGE_CENTS = 150;

    private final long id;
    private final String film;
    private final int runtimeMinutes;
    private final int basePriceCents;

    public Screening(long id, String film, int runtimeMinutes, int basePriceCents) {
        this.id = id;
        this.film = film;
        this.runtimeMinutes = runtimeMinutes;
        this.basePriceCents = basePriceCents;
    }

    public long getId() { return id; }

    public String getFilm() { return film; }

    public int getRuntimeMinutes() { return runtimeMinutes; }

    public int getBasePriceCents() { return basePriceCents; }

    /** How many started 30-minute blocks the film runs beyond 120 minutes (0 for a film of 120 minutes or less). */
    public int surchargeBlocks() {
        int over = runtimeMinutes - INCLUDED_MINUTES;
        if (over <= 0) return 0;
        return (over - 1) / BLOCK_MINUTES + 1;
    }

    /** The price of one seat: the base price plus 150 cents per surcharge block. */
    public int seatPriceCents() {
        return basePriceCents + surchargeBlocks() * SURCHARGE_CENTS;
    }
}
