package edu.practice.d11.dto;

import java.util.List;

/** The answer to a preview: which requests WOULD be accepted, and which rejected (both in request order). */
public record Plan(List<BookingRequest> accepted, List<Rejection> rejected) { }
