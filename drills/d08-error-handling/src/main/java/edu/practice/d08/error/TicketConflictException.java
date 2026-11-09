package edu.practice.d08.error;

/**
 * Thrown when the request is valid but clashes with what is already stored:
 * here, booking a seat somebody else already holds. Should become HTTP 409.
 */
public class TicketConflictException extends RuntimeException {
    public TicketConflictException(long id) {
        super("Ticket " + id + " is already booked");
    }
}
