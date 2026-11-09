package edu.practice.d11.dto;

import java.util.List;

/**
 * POST /api/rooms/commit body, e.g. {"key":"team-sync-1","bookings":[{"room":"ASPEN","start":11,"end":12}]}.
 * `key` is the idempotency key: the client makes it up once per action and sends the same key on every retry.
 *
 * A record, so equals() compares the key AND the whole bookings list, element by element.
 */
public record CommitRequest(String key, List<BookingRequest> bookings) { }
