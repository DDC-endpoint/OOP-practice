package edu.practice.d11.dto;

import java.util.List;

/** POST /api/rooms/preview body, e.g. {"bookings":[{"room":"ASPEN","start":11,"end":12},{"room":"BIRCH","start":9,"end":10}]}. */
public record PreviewRequest(List<BookingRequest> bookings) { }
