package edu.practice.d11;

import edu.practice.d11.dto.BookingRequest;
import edu.practice.d11.dto.CommitRequest;
import edu.practice.d11.dto.CommitResult;
import edu.practice.d11.dto.Plan;
import edu.practice.d11.dto.Rejection;
import edu.practice.d11.error.CommitConflictException;
import edu.practice.d11.error.RoomNotFoundException;
import edu.practice.d11.model.Booking;
import edu.practice.d11.repo.BookingRepository;
import edu.practice.d11.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static edu.practice.d11.service.BookingService.CLASHES_WITH_CONFIRMED;
import static edu.practice.d11.service.BookingService.CLASHES_WITH_EARLIER_REQUEST;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'BookingServiceTest'.
 *
 * No Spring here: each test builds a fresh repository and service with `new`, so every test
 * starts with the 4 seeded bookings and empty idempotency maps, whatever order the tests run in:
 *
 *   id 1  BIRCH [13,15)      id 2  ASPEN [14,16)      id 3  ASPEN [9,11)      id 4  CEDAR [10,12)
 */
class BookingServiceTest {

    private static final Set<Booking> SEEDED = Set.of(
            new Booking(1, "BIRCH", 13, 15), new Booking(2, "ASPEN", 14, 16),
            new Booking(3, "ASPEN", 9, 11), new Booking(4, "CEDAR", 10, 12));

    private BookingRepository repo;
    private BookingService service;

    @BeforeEach
    void freshServiceForEveryTest() {
        repo = new BookingRepository();
        service = new BookingService(repo);
    }

    // ---- helpers ----

    private static BookingRequest req(String room, int start, int end) {
        return new BookingRequest(room, start, end);
    }

    private static Rejection rej(String room, int start, int end, String reason) {
        return new Rejection(room, start, end, reason);
    }

    private Set<Booking> confirmedNow() {
        return Set.copyOf(repo.all());
    }

    private static List<Long> ids(List<Booking> bookings) {
        return bookings.stream().map(Booking::id).toList();
    }

    private void assertOverlap(boolean expected, int aStart, int aEnd, int bStart, int bEnd, String why) {
        assertEquals(expected, BookingService.overlaps(aStart, aEnd, bStart, bEnd),
                "overlaps(" + aStart + "," + aEnd + ", " + bStart + "," + bEnd + "): [" + aStart + "," + aEnd + ") and ["
                        + bStart + "," + bEnd + ") " + why);
    }

    private void assertPlan(Plan plan, List<BookingRequest> accepted, List<Rejection> rejected, String what) {
        assertNotNull(plan, what + ": preview returned null");
        assertEquals(accepted, plan.accepted(), what + ": accepted (in request order)");
        assertEquals(rejected, plan.rejected(), what + ": rejected (in request order, with the reason)");
    }

    // ---- overlaps ----

    @Test
    @DisplayName("1. overlaps(): intervals that only touch do NOT overlap (half-open)")
    void touchingDoesNotOverlap() {
        assertOverlap(false, 9, 11, 11, 12, "only touch at 11:00");
        assertOverlap(false, 11, 12, 9, 11, "only touch at 11:00 (other way round)");
        assertOverlap(false, 13, 15, 12, 13, "only touch at 13:00");
        assertOverlap(false, 15, 16, 13, 15, "only touch at 15:00");
        assertOverlap(false, 8, 9, 17, 18, "are far apart");
        assertOverlap(false, 17, 18, 8, 17, "only touch at 17:00");
        assertOverlap(false, 8, 10, 12, 14, "have a gap between them");
    }

    @Test
    @DisplayName("2. overlaps(): intervals that share at least one hour DO overlap")
    void sharingAnHourOverlaps() {
        assertOverlap(true, 9, 11, 10, 12, "share hour 10");
        assertOverlap(true, 10, 12, 9, 11, "share hour 10 (other way round)");
        assertOverlap(true, 9, 12, 10, 11, "one is inside the other");
        assertOverlap(true, 10, 11, 9, 12, "one is inside the other (other way round)");
        assertOverlap(true, 9, 10, 9, 10, "are identical");
        assertOverlap(true, 13, 15, 14, 18, "share hour 14");
        assertOverlap(true, 8, 18, 17, 18, "share hour 17");
    }

    // ---- bookings() ordering ----

    @Test
    @DisplayName("3. bookings(): room A to Z, then earliest start, then lowest id")
    void bookingsOrder() {
        assertEquals(List.of(3L, 2L, 1L, 4L), ids(service.bookings()), "the 4 seeded bookings: ASPEN 9, ASPEN 14, BIRCH 13, CEDAR 10");
        assertEquals(new Booking(3, "ASPEN", 9, 11), service.bookings().get(0));

        repo.save(new Booking(9, "ASPEN", 12, 13));
        repo.save(new Booking(7, "CEDAR", 8, 9));
        repo.save(new Booking(8, "BIRCH", 16, 18));
        repo.save(new Booking(11, "BIRCH", 8, 9));
        repo.save(new Booking(10, "BIRCH", 8, 10));   // same room and start as 11: lower id first
        assertEquals(List.of(3L, 9L, 2L, 10L, 11L, 1L, 8L, 7L, 4L), ids(service.bookings()),
                "after adding 5 more bookings: room A to Z, then start, then id");
        assertEquals(9, repo.all().size(), "bookings() must only read, never add or remove");
    }

    // ---- preview ----

    @Test
    @DisplayName("4. preview(): free slots are accepted; a slot that overlaps a confirmed booking in the same room is rejected")
    void previewAgainstConfirmed() {
        Plan plan = service.preview(List.of(
                req("CEDAR", 12, 14), req("CEDAR", 9, 11), req("ASPEN", 16, 18),
                req("ASPEN", 8, 9), req("BIRCH", 14, 15), req("BIRCH", 10, 13)));
        assertPlan(plan,
                List.of(req("CEDAR", 12, 14), req("ASPEN", 16, 18), req("ASPEN", 8, 9), req("BIRCH", 10, 13)),
                List.of(rej("CEDAR", 9, 11, CLASHES_WITH_CONFIRMED), rej("BIRCH", 14, 15, CLASHES_WITH_CONFIRMED)),
                "preview of 6 requests");

        assertPlan(service.preview(List.of(req("ASPEN", 11, 14))),
                List.of(req("ASPEN", 11, 14)), List.of(),
                "ASPEN [11,14) fits exactly between [9,11) and [14,16)");
        assertPlan(service.preview(List.of(req("ASPEN", 10, 15))),
                List.of(), List.of(rej("ASPEN", 10, 15, CLASHES_WITH_CONFIRMED)),
                "ASPEN [10,15) overlaps both confirmed ASPEN bookings");
    }

    @Test
    @DisplayName("5. preview(): a request that overlaps an EARLIER accepted request in the same preview is rejected")
    void previewAgainstEarlierRequests() {
        Plan plan = service.preview(List.of(
                req("BIRCH", 9, 12), req("BIRCH", 11, 13), req("CEDAR", 12, 13), req("CEDAR", 13, 14),
                req("ASPEN", 12, 14), req("ASPEN", 13, 14), req("ASPEN", 11, 12)));
        assertPlan(plan,
                List.of(req("BIRCH", 9, 12), req("CEDAR", 12, 13), req("CEDAR", 13, 14), req("ASPEN", 12, 14), req("ASPEN", 11, 12)),
                List.of(rej("BIRCH", 11, 13, CLASHES_WITH_EARLIER_REQUEST), rej("ASPEN", 13, 14, CLASHES_WITH_EARLIER_REQUEST)),
                "preview of 7 requests");

        assertPlan(service.preview(List.of(req("BIRCH", 16, 17), req("BIRCH", 16, 17))),
                List.of(req("BIRCH", 16, 17)), List.of(rej("BIRCH", 16, 17, CLASHES_WITH_EARLIER_REQUEST)),
                "the same request twice: the second one clashes with the first");
    }

    @Test
    @DisplayName("6. preview(): confirmed clashes are reported first, rejected requests block nothing, other rooms never clash")
    void previewDetails() {
        assertPlan(service.preview(List.of(req("CEDAR", 12, 14), req("CEDAR", 11, 13))),
                List.of(req("CEDAR", 12, 14)), List.of(rej("CEDAR", 11, 13, CLASHES_WITH_CONFIRMED)),
                "CEDAR [11,13) overlaps BOTH the confirmed [10,12) and the earlier [12,14): check confirmed first");

        assertPlan(service.preview(List.of(req("ASPEN", 10, 12), req("ASPEN", 11, 12))),
                List.of(req("ASPEN", 11, 12)), List.of(rej("ASPEN", 10, 12, CLASHES_WITH_CONFIRMED)),
                "ASPEN [10,12) is rejected, so it must NOT block ASPEN [11,12)");

        assertPlan(service.preview(List.of(req("ASPEN", 12, 13), req("BIRCH", 12, 13), req("CEDAR", 12, 13))),
                List.of(req("ASPEN", 12, 13), req("BIRCH", 12, 13), req("CEDAR", 12, 13)), List.of(),
                "the same hours in three DIFFERENT rooms");
    }

    @Test
    @DisplayName("7. preview() changes nothing: same confirmed bookings after two previews, and both previews agree")
    void previewDoesNotMutate() {
        List<BookingRequest> requests = List.of(req("ASPEN", 11, 12), req("BIRCH", 8, 10), req("BIRCH", 9, 10));
        Plan first = service.preview(requests);
        Plan second = service.preview(requests);

        assertPlan(first, List.of(req("ASPEN", 11, 12), req("BIRCH", 8, 10)),
                List.of(rej("BIRCH", 9, 10, CLASHES_WITH_EARLIER_REQUEST)), "first preview");
        assertEquals(first, second, "previewing the same requests twice must give the same plan. "
                + "If the second one rejects more, the first preview saved something.");
        assertEquals(SEEDED, confirmedNow(), "preview must not add, change or remove confirmed bookings");
        assertEquals(5, repo.nextId(), "preview must not even use up booking ids (only commit creates bookings)");
    }

    // ---- commit ----

    @Test
    @DisplayName("8. commit() with a new key saves the accepted bookings with new ids and returns them")
    void commitNewKey() {
        CommitResult r = service.commit(new CommitRequest("k-1",
                List.of(req("ASPEN", 11, 12), req("ASPEN", 10, 12), req("CEDAR", 14, 17))));
        assertEquals("k-1", r.key());
        assertEquals(List.of(new Booking(5, "ASPEN", 11, 12), new Booking(6, "CEDAR", 14, 17)), r.booked(),
                "the accepted requests become bookings with ids from repo.nextId() (5, 6, ...), in request order");
        assertEquals(List.of(rej("ASPEN", 10, 12, CLASHES_WITH_CONFIRMED)), r.rejected());
        assertEquals(6, repo.all().size(), "4 seeded + 2 committed");
        assertTrue(repo.all().containsAll(r.booked()), "the booked bookings must be saved in the repository");

        CommitResult r2 = service.commit(new CommitRequest("k-2", List.of(req("BIRCH", 8, 10), req("ASPEN", 11, 13))));
        assertEquals(List.of(new Booking(7, "BIRCH", 8, 10)), r2.booked());
        assertEquals(List.of(rej("ASPEN", 11, 13, CLASHES_WITH_CONFIRMED)), r2.rejected(),
                "ASPEN [11,13) overlaps ASPEN [11,12), which k-1 confirmed");
        assertEquals(7, repo.all().size());
    }

    @Test
    @DisplayName("9. commit() retried with the same key and an equal request returns the stored result and saves nothing new")
    void commitRetryIsIdempotent() {
        CommitResult first = service.commit(new CommitRequest("pay-77", List.of(req("BIRCH", 15, 17), req("CEDAR", 8, 10))));
        assertEquals(List.of(new Booking(5, "BIRCH", 15, 17), new Booking(6, "CEDAR", 8, 10)), first.booked());
        assertEquals(6, repo.all().size());

        // A retry arrives as a NEW object with the same values (like a second HTTP request would).
        List<BookingRequest> again = new ArrayList<>();
        again.add(new BookingRequest("BIRCH", 15, 17));
        again.add(new BookingRequest("CEDAR", 8, 10));
        CommitResult retry = service.commit(new CommitRequest("pay-77", again));
        assertEquals(first, retry, "a retry must get exactly the first result back (compare requests with equals, not ==)");
        assertEquals(6, repo.all().size(), "a retry must not save anything again");

        service.commit(new CommitRequest("other", List.of(req("ASPEN", 16, 18))));
        assertEquals(7, repo.all().size());
        CommitResult later = service.commit(new CommitRequest("pay-77", List.of(req("BIRCH", 15, 17), req("CEDAR", 8, 10))));
        assertEquals(first, later, "even after other commits, the retry gets the ORIGINAL result, not a recalculated one");
        assertEquals(List.of(5L, 6L), ids(later.booked()));
        assertEquals(7, repo.all().size());
    }

    @Test
    @DisplayName("10. commit() with a used key but a different request -> CommitConflictException, nothing saved")
    void commitConflict() {
        CommitResult first = service.commit(new CommitRequest("k-9", List.of(req("ASPEN", 11, 12))));
        assertEquals(5, repo.all().size());

        CommitConflictException e = assertThrows(CommitConflictException.class,
                () -> service.commit(new CommitRequest("k-9", List.of(req("ASPEN", 11, 13)))),
                "same key, different hours -> CommitConflictException");
        assertTrue(e.getMessage().contains("k-9"), "use new CommitConflictException(key) so the message names the key");
        assertThrows(CommitConflictException.class,
                () -> service.commit(new CommitRequest("k-9", List.of(req("ASPEN", 11, 12), req("BIRCH", 8, 9)))),
                "same key, one extra booking -> CommitConflictException");
        assertThrows(CommitConflictException.class,
                () -> service.commit(new CommitRequest("k-9", List.of(req("CEDAR", 11, 12)))),
                "same key, different room -> CommitConflictException");
        assertEquals(5, repo.all().size(), "a conflicting commit must not save anything");

        assertEquals(first, service.commit(new CommitRequest("k-9", List.of(req("ASPEN", 11, 12)))),
                "the key still belongs to the ORIGINAL request: retrying it still works");
    }

    // ---- validation ----

    @Test
    @DisplayName("11. preview(): bad hours -> IllegalArgumentException, unknown room -> RoomNotFoundException")
    void previewValidation() {
        for (BookingRequest bad : List.of(req("ASPEN", 7, 9), req("ASPEN", 12, 12), req("ASPEN", 15, 14),
                req("CEDAR", 17, 19), req("BIRCH", -1, 3))) {
            assertThrows(IllegalArgumentException.class, () -> service.preview(List.of(bad)),
                    "preview of " + bad + " should throw IllegalArgumentException: hours must satisfy 8 <= start < end <= 18");
        }
        assertThrows(IllegalArgumentException.class, () -> service.preview(List.of()), "an empty list -> IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> service.preview(List.of(req("  ", 9, 10))), "a blank room -> IllegalArgumentException");

        RoomNotFoundException e = assertThrows(RoomNotFoundException.class,
                () -> service.preview(List.of(req("ASPEN", 12, 13), req("ZEUS", 9, 10))));
        assertEquals("Room not found: ZEUS", e.getMessage());

        assertPlan(service.preview(List.of(req("ASPEN", 8, 9), req("CEDAR", 17, 18))),
                List.of(req("ASPEN", 8, 9), req("CEDAR", 17, 18)), List.of(),
                "8 and 18 are allowed: the opening hours are [8,18)");
        assertEquals(SEEDED, confirmedNow());
    }

    @Test
    @DisplayName("12. a commit that fails validation saves NOTHING (all or nothing) and does not use up its key")
    void commitValidationIsAtomic() {
        assertThrows(IllegalArgumentException.class,
                () -> service.commit(new CommitRequest("k-bad", List.of(req("ASPEN", 11, 12), req("ASPEN", 17, 19)))),
                "the second request has bad hours -> IllegalArgumentException");
        assertThrows(RoomNotFoundException.class,
                () -> service.commit(new CommitRequest("k-room", List.of(req("BIRCH", 8, 9), req("ORION", 9, 10)))),
                "the second request names an unknown room -> RoomNotFoundException");
        assertEquals(SEEDED, confirmedNow(), "the VALID first requests must not be saved either: a commit is all or nothing");

        assertThrows(IllegalArgumentException.class,
                () -> service.commit(new CommitRequest("  ", List.of(req("ASPEN", 11, 12)))), "a blank key -> IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> service.commit(new CommitRequest(null, List.of(req("ASPEN", 11, 12)))), "a missing key -> IllegalArgumentException");
        assertEquals(SEEDED, confirmedNow());

        CommitResult ok = service.commit(new CommitRequest("k-bad", List.of(req("ASPEN", 11, 12))));
        assertEquals(List.of(new Booking(5, "ASPEN", 11, 12)), ok.booked(),
                "a failed commit must not store its key: a corrected request with the same key is a NEW commit");
    }
}
