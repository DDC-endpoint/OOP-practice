package edu.practice.d11.error;

import edu.practice.d11.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Given and finished (you wrote one of these in d08). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiError> notFound(RoomNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(CommitConflictException.class)
    public ResponseEntity<ApiError> conflict(CommitConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT", e.getMessage()));
    }
}
