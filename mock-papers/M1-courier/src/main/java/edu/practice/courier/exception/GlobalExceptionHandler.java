package edu.practice.courier.exception;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e){
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BAD_REQUEST",e.getMessage()));
 }
 @ExceptionHandler(ParcelNotFoundException.class)
 public ResponseEntity<ErrorResponse> notFound(ParcelNotFoundException e){
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("NOT_FOUND",e.getMessage()));
 }
 @ExceptionHandler(DuplicateParcelException.class)
 public ResponseEntity<ErrorResponse> conflict(DuplicateParcelException e){
  return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("CONFLICT",e.getMessage()));
 }
}
