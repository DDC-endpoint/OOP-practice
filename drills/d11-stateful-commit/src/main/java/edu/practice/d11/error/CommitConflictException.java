package edu.practice.d11.error;

/** The idempotency key was already used for a DIFFERENT request. Becomes HTTP 409 CONFLICT. */
public class CommitConflictException extends RuntimeException {
    public CommitConflictException(String key) {
        super("Key '" + key + "' was already used for a different request");
    }
}
