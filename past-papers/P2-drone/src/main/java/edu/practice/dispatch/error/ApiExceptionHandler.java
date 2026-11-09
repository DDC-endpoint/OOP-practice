package edu.practice.dispatch.error;
import edu.practice.dispatch.dto.ApiError;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(MissionNotFoundException.class)
 public ResponseEntity<ApiError> notFound(MissionNotFoundException ex){
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND",ex.getMessage()));
 }
 // ArithmeticException = an energy overflow detected by Math.toIntExact: also invalid input.
 @ExceptionHandler({IllegalArgumentException.class,ArithmeticException.class})
 public ResponseEntity<ApiError> badRequest(RuntimeException ex){
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("BAD_REQUEST",ex.getMessage()));
 }
}
