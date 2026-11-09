package edu.practice.scheduler.error;
import edu.practice.scheduler.dto.ApiError;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class ApiExceptionHandler {
 // ArithmeticException = an overflow detected by Math.toIntExact: also invalid input.
 @ExceptionHandler({IllegalArgumentException.class,ArithmeticException.class})
 public ResponseEntity<ApiError> badRequest(RuntimeException ex){
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("BAD_REQUEST",ex.getMessage()));
 }
 @ExceptionHandler(WorkloadNotFoundException.class)
 public ResponseEntity<ApiError> notFound(WorkloadNotFoundException ex){
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND",ex.getMessage()));
 }
 @ExceptionHandler(CommitConflictException.class)
 public ResponseEntity<ApiError> conflict(CommitConflictException ex){
  return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT",ex.getMessage()));
 }
}
