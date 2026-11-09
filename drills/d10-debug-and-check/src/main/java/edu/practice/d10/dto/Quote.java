package edu.practice.d10.dto;

/** The price of some seats at one screening, e.g. {"screeningId":3,"film":"Paper Kingdoms","seats":2,"seatPriceCents":1100,"totalCents":2200}. */
public record Quote(long screeningId, String film, int seats, int seatPriceCents, int totalCents) { }
