package edu.practice.d08.dto;

/** The JSON body of every error response, e.g. {"code":"NOT_FOUND","message":"Ticket not found: 9"}. */
public record ApiError(String code, String message) { }
