package edu.practice.courier.exception;
/** The JSON body of every error, e.g. {"code":"NOT_FOUND","message":"Parcel not found: 99"}. A plain class: Jackson reads the getters. */
public class ErrorResponse {
 private final String code; private final String message;
 public ErrorResponse(String code,String message){this.code=code;this.message=message;}
 public String getCode(){return code;}
 public String getMessage(){return message;}
}
