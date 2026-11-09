package edu.practice.parking.error;
import edu.practice.parking.dto.ApiError;import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(PassNotFoundException.class)
 public ResponseEntity<ApiError> notFound(PassNotFoundException ex){
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND",ex.getMessage()));
 }
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex){
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("BAD_REQUEST",ex.getMessage()));
 }
}
