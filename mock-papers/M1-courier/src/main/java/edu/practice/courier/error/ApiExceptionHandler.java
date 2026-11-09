package edu.practice.courier.error;
import edu.practice.courier.dto.ApiError;import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class ApiExceptionHandler {
 // TODO Task 3: map IllegalArgumentException -> HTTP 400, ParcelNotFoundException -> HTTP 404 and
 // DuplicateParcelException -> HTTP 409. Each body is an ApiError with BAD_REQUEST, NOT_FOUND or CONFLICT.
}
