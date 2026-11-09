package edu.practice.d08.dto;

/** POST /api/tickets body, e.g. {"id":10,"event":"Opera Gala","seat":"C3","priceCents":4500}. */
public record NewTicket(long id, String event, String seat, int priceCents) { }
