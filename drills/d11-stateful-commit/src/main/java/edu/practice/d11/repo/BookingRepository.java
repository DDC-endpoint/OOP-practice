package edu.practice.d11.repo;

import edu.practice.d11.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory storage of the CONFIRMED bookings, the same shape as the LBP starters' repositories. Given. */
@Repository
public class BookingRepository {

    private static final Set<String> ROOMS = Set.of("ASPEN", "BIRCH", "CEDAR");

    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(5);

    public BookingRepository() {
        save(new Booking(1, "BIRCH", 13, 15));
        save(new Booking(2, "ASPEN", 14, 16));
        save(new Booking(3, "ASPEN", 9, 11));
        save(new Booking(4, "CEDAR", 10, 12));
    }

    public boolean roomExists(String room) { return room != null && ROOMS.contains(room); }

    /** A read-only COPY of every confirmed booking, in no particular order. */
    public List<Booking> all() { return List.copyOf(bookings.values()); }

    /** A fresh id for a new booking: 5, then 6, 7, ... Never hands out the same id twice. */
    public long nextId() { return nextId.getAndIncrement(); }

    public void save(Booking booking) { bookings.put(booking.id(), booking); }
}
