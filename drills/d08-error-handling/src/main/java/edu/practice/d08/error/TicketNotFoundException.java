package edu.practice.d08.error;

/** Thrown when no ticket has this id. Should become HTTP 404. */
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(long id) {
        super("Ticket not found: " + id);
    }
}
