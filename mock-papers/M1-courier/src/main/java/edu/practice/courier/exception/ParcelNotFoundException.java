package edu.practice.courier.exception;
public class ParcelNotFoundException extends RuntimeException {
 public ParcelNotFoundException(long id){super("Parcel not found: "+id);}
}
