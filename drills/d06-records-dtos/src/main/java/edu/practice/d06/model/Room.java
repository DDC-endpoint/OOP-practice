package edu.practice.d06.model;

/**
 * Parent of every hotel room. Given: don't change it.
 *
 * This is the MODEL: everything the hotel knows about a room, including things a guest
 * never needs to see (capacity rules, rates, fees). The API sends a RoomQuote instead.
 */
public abstract class Room {

    private final long id;
    private final String number;
    private final int capacity;

    protected Room(long id, String number, int capacity) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("number is required");
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.id = id;
        this.number = number.trim();
        this.capacity = capacity;
    }

    public long getId() { return id; }

    /** The number on the door, e.g. "301". */
    public String getNumber() { return number; }

    /** The most guests who can sleep in this room. */
    public int getCapacity() { return capacity; }

    /** A short label for this kind of room, e.g. "SUITE". */
    public abstract String type();

    /** Total price in whole cents for a stay of `nights` nights. Rejects nights <= 0. */
    public abstract int priceCents(int nights);
}
