package edu.practice.d11.dto;

/** One requested booking, the half-open interval [start, end) in whole hours, e.g. {"room":"ASPEN","start":11,"end":12}. */
public record BookingRequest(String room, int start, int end) { }
