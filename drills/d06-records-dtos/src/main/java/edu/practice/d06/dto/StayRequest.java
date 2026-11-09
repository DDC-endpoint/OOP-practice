package edu.practice.d06.dto;

/**
 * Given. What a guest asks for: "a room for `guests` people, for `nights` nights".
 * In a Spring app this would arrive as JSON: {"nights": 3, "guests": 2}.
 *
 * Read it as an example of a record:
 *   - the header (int nights, int guests) IS the list of fields; Java writes the constructor,
 *     the accessors nights() and guests(), equals, hashCode and toString for you;
 *   - the block below is a COMPACT constructor: it runs before the fields are set,
 *     so bad data never becomes an object.
 */
public record StayRequest(int nights, int guests) {

    public StayRequest {
        if (nights <= 0) throw new IllegalArgumentException("nights must be positive");
        if (guests <= 0) throw new IllegalArgumentException("guests must be positive");
    }
}
