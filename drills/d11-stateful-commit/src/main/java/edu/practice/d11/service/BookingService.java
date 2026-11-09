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
        // TODO d11-1: ONE line. [9,11) and [11,12) only touch, so they do NOT overlap.
        //             [9,11) and [10,12) share hour 10, so they DO.
        throw new UnsupportedOperationException("d11: write overlaps(...)");
    }

    /** Every confirmed booking: room A to Z, then earliest start first, then lowest id first. */
    public List<Booking> bookings() {
        // TODO d11-2: sort repo.all() with a Comparator chain (d05): room, then start, then id. Return the sorted list.
        throw new UnsupportedOperationException("d11: write bookings()");
    }

    /** What WOULD happen to these requests, in order, if they were committed now. Changes nothing. */
    public synchronized Plan preview(List<BookingRequest> requests) {
        // TODO d11-3:
        //   1. validate(requests);   (given, below: throws for 400 / 404)
        //   2. Read the confirmed bookings: repo.all()
        //   3. Make two NEW local lists: accepted (BookingRequest) and rejected (Rejection).
        //   4. For each request r, in order:
        //        - same room AND overlaps a confirmed booking  -> rejected, reason CLASHES_WITH_CONFIRMED
        //        - else same room AND overlaps an ACCEPTED one  -> rejected, reason CLASHES_WITH_EARLIER_REQUEST
        //        - else                                         -> accepted
        //   5. return new Plan(accepted, rejected)
        //   Never call repo.save(...) or repo.nextId() here: a preview must change NOTHING.
        throw new UnsupportedOperationException("d11: write preview(...)");
    }

    /** Saves the accepted bookings, once per key. A retry with the same key and request gets the stored result back. */
    public synchronized CommitResult commit(CommitRequest request) {
        // TODO d11-4:
        //   1. A null request, or a null/blank key -> throw new IllegalArgumentException("key is required")
        //   2. Has this key been used before (requestsByKey)?
        //        - with an EQUAL request (records: use equals) -> return the STORED result. Decide nothing, save nothing.
        //        - with a different request                   -> throw new CommitConflictException(key)   (409)
        //   3. A new key: Plan plan = preview(request.bookings());   (validates, decides, changes nothing)
        //   4. Turn each accepted request into new Booking(repo.nextId(), room, start, end), and save them all.
        //   5. Build the CommitResult(key, booked, plan.rejected()), store request AND result under the key, return it.
        throw new UnsupportedOperationException("d11: write commit(...)");
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
