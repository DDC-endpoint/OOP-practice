package edu.practice.courier.exception;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 // TODO Task 3: map IllegalArgumentException -> HTTP 400, ParcelNotFoundException -> HTTP 404 and
 // DuplicateParcelException -> HTTP 409. Each body is an ErrorResponse with BAD_REQUEST, NOT_FOUND or CONFLICT.
}
