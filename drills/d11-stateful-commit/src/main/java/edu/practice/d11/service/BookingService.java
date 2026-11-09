package edu.practice.d11.service;

import edu.practice.d11.dto.BookingRequest;
import edu.practice.d11.dto.CommitRequest;
import edu.practice.d11.dto.CommitResult;
import edu.practice.d11.dto.Plan;
import edu.practice.d11.dto.Rejection;
import edu.practice.d11.error.CommitConflictException;
import edu.practice.d11.error.RoomNotFoundException;
import edu.practice.d11.model.Booking;
import edu.practice.d11.repo.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Meeting-room bookings: preview (changes nothing) and commit (saves, once per idempotency key).
 * Every interval is half-open: [start, end) in whole hours.
 */
@Service
public class BookingService {

    /** The building opens at 8:00 and closes at 18:00, so every booking must satisfy 8 <= start < end <= 18. */
    public static final int OPENS = 8;
    public static final int CLOSES = 18;

    /** Rejection reasons. */
    public static final String CLASHES_WITH_CONFIRMED = "CLASHES_WITH_CONFIRMED";
    public static final String CLASHES_WITH_EARLIER_REQUEST = "CLASHES_WITH_EARLIER_REQUEST";

    private final BookingRepository repo;

    /** Idempotency memory. key -> the request first committed under that key. */
    private final Map<String, CommitRequest> requestsByKey = new HashMap<>();
    /** key -> the result that request got. A retry with the same key and request gets exactly this back. */
    private final Map<String, CommitResult> resultsByKey = new HashMap<>();

    public BookingService(BookingRepository repo) {
        this.repo = repo;
    }

    /** Do the half-open intervals [aStart, aEnd) and [bStart, bEnd) share at least one hour? */
    public static boolean overlaps(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && bStart < aEnd;
    }

    /** Every confirmed booking: room A to Z, then earliest start first, then lowest id first. */
    public List<Booking> bookings() {
        return repo.all().stream()
                .sorted(Comparator.comparing(Booking::room)
                        .thenComparingInt(Booking::start)
                        .thenComparingLong(Booking::id))
                .toList();
    }

    /** What WOULD happen to these requests, in order, if they were committed now. Changes nothing. */
    public synchronized Plan preview(List<BookingRequest> requests) {
        validate(requests);
        List<Booking> confirmed = repo.all();
        List<BookingRequest> accepted = new ArrayList<>();
        List<Rejection> rejected = new ArrayList<>();
        for (BookingRequest r : requests) {
            boolean clashesWithConfirmed = confirmed.stream()
                    .anyMatch(b -> b.room().equals(r.room()) && overlaps(r.start(), r.end(), b.start(), b.end()));
            boolean clashesWithEarlier = accepted.stream()
                    .anyMatch(a -> a.room().equals(r.room()) && overlaps(r.start(), r.end(), a.start(), a.end()));
            if (clashesWithConfirmed) {
                rejected.add(new Rejection(r.room(), r.start(), r.end(), CLASHES_WITH_CONFIRMED));
            } else if (clashesWithEarlier) {
                rejected.add(new Rejection(r.room(), r.start(), r.end(), CLASHES_WITH_EARLIER_REQUEST));
            } else {
                accepted.add(r);
            }
        }
        return new Plan(List.copyOf(accepted), List.copyOf(rejected));
    }

    /** Saves the accepted bookings, once per key. A retry with the same key and request gets the stored result back. */
    public synchronized CommitResult commit(CommitRequest request) {
        if (request == null || request.key() == null || request.key().isBlank()) {
            throw new IllegalArgumentException("key is required");
        }
        CommitRequest earlier = requestsByKey.get(request.key());
        if (earlier != null) {
            if (earlier.equals(request)) {
                return resultsByKey.get(request.key());
            }
            throw new CommitConflictException(request.key());
        }
        Plan plan = preview(request.bookings());
        List<Booking> booked = new ArrayList<>();
        for (BookingRequest r : plan.accepted()) {
            booked.add(new Booking(repo.nextId(), r.room(), r.start(), r.end()));
        }
        for (Booking b : booked) {
            repo.save(b);
        }
        CommitResult result = new CommitResult(request.key(), List.copyOf(booked), plan.rejected());
        requestsByKey.put(request.key(), request);
        resultsByKey.put(request.key(), result);
        return result;
    }

    /**
     * Given: checks the WHOLE request before anything is decided or saved.
     * Throws IllegalArgumentException (400) for a missing list, a blank room or bad hours,
     * and RoomNotFoundException (404) for a room that doesn't exist.
     */
    private void validate(List<BookingRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("at least one booking is required");
        }
        for (BookingRequest r : requests) {
            if (r == null || r.room() == null || r.room().isBlank()) {
                throw new IllegalArgumentException("room is required");
            }
            if (r.start() < OPENS || r.end() > CLOSES || r.start() >= r.end()) {
                throw new IllegalArgumentException("hours must satisfy " + OPENS + " <= start < end <= " + CLOSES
                        + ", got [" + r.start() + "," + r.end() + ")");
            }
            if (!repo.roomExists(r.room())) {
                throw new RoomNotFoundException(r.room());
            }
        }
    }
}
