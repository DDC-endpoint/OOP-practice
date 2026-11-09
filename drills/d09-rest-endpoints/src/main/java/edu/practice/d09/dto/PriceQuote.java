package edu.practice.d09.dto;

/** GET /api/plants/{id}/price?qty=3 answer, e.g. {"plantId":1,"name":"Lavender","qty":3,"totalCents":1350}. */
public record PriceQuote(long plantId, String name, int qty, int totalCents) { }
