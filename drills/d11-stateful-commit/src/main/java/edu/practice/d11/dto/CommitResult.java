package edu.practice.d11.dto;

import edu.practice.d11.model.Booking;

import java.util.List;

/** The answer to a commit: the bookings that were really saved (with their new ids), and the rejected requests. */
public record CommitResult(String key, List<Booking> booked, List<Rejection> rejected) { }
