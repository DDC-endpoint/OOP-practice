package edu.practice.d10.error;

public class ScreeningNotFoundException extends RuntimeException {
    public ScreeningNotFoundException(long id) {
        super("Screening not found: " + id);
    }
}
