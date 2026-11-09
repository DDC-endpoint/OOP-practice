package edu.practice.d08.error;

import edu.practice.d08.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Catches exceptions thrown by ANY controller (and the services they call)
 * and turns them into an HTTP status + an ApiError JSON body.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // TODO d08-1: IllegalArgumentException   -> HTTP 400, body new ApiError("BAD_REQUEST", <the exception's message>)
    // TODO d08-2: TicketNotFoundException    -> HTTP 404, body new ApiError("NOT_FOUND", <the exception's message>)
    // TODO d08-3: TicketConflictException    -> HTTP 409, body new ApiError("CONFLICT", <the exception's message>)
    //
    // One public method per exception. Each one:
    //   - is annotated @ExceptionHandler(TheException.class)
    //   - takes the exception as its parameter
    //   - returns ResponseEntity<ApiError>
}
