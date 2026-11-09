package edu.practice.courier.error;
public class ParcelNotFoundException extends RuntimeException {
 public ParcelNotFoundException(long id){super("Parcel not found: "+id);}
}
