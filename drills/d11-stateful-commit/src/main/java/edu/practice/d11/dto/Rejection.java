package edu.practice.d11.dto;

/** A requested booking that was turned down, and why, e.g. {"room":"ASPEN","start":10,"end":12,"reason":"CLASHES_WITH_CONFIRMED"}. */
public record Rejection(String room, int start, int end, String reason) { }
