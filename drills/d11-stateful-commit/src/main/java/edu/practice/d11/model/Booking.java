package edu.practice.d11.model;

/**
 * A CONFIRMED booking: room `room` is taken for the half-open interval [start, end) in whole hours.
 * [9,11) means from 9:00 up to (but not including) 11:00, so it covers hours 9 and 10.
 *
 * A record, because a confirmed booking never changes, and records give us equals() for free.
 * JSON: {"id":3,"room":"ASPEN","start":9,"end":11}
 */
public record Booking(long id, String room, int start, int end) { }
