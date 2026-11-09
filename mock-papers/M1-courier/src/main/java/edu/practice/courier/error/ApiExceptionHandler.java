package edu.practice.courier.error;
import edu.practice.courier.dto.ApiError;import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ApiError> badRequest(IllegalArgumentException e){
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("BAD_REQUEST",e.getMessage()));
 }
 @ExceptionHandler(ParcelNotFoundException.class)
 public ResponseEntity<ApiError> notFound(ParcelNotFoundException e){
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND",e.getMessage()));
 }
 @ExceptionHandler(DuplicateParcelException.class)
 public ResponseEntity<ApiError> conflict(DuplicateParcelException e){
  return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT",e.getMessage()));
 }
}
