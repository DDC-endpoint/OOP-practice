package edu.practice.d08.model;

/** One seat at one event. Given: don't change it. */
public class Ticket {

    private final long id;
    private final String event;
    private final String seat;
    private final int priceCents;
    private String heldBy;          // null while the seat is still free

    public Ticket(long id, String event, String seat, int priceCents) {
        this.id = id;
        this.event = event;
        this.seat = seat;
        this.priceCents = priceCents;
    }

    public long getId() { return id; }

    public String getEvent() { return event; }

    public String getSeat() { return seat; }

    public int getPriceCents() { return priceCents; }

    public String getHeldBy() { return heldBy; }

    public boolean isBooked() { return heldBy != null; }

    public void bookFor(String customer) { this.heldBy = customer; }
}
