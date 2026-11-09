package edu.practice.d08.dto;

/** POST /api/tickets/{id}/book body, e.g. {"customer":"Ana"}. */
public record BookingRequest(String customer) { }
