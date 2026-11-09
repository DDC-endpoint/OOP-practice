package edu.practice.d06.dto;

/**
 * What the API sends back for one room: "room 301, a SUITE, 3 nights, costs 37,000".
 * As JSON: {"roomId":3,"number":"301","type":"SUITE","nights":3,"totalCents":37000}
 */
public record RoomQuote(long roomId, String number, String type, int nights, int totalCents) {

    public RoomQuote {
        if (number == null || number.isBlank()) throw new IllegalArgumentException("number is required");
        if (nights <= 0) throw new IllegalArgumentException("nights must be positive");
        if (totalCents < 0) throw new IllegalArgumentException("totalCents cannot be negative");
    }
}
