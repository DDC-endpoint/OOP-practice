package edu.practice.courier.model;
public class StandardParcel extends Parcel {
 // TODO Task 1: declare a private final int ratePerBlockPence field and a public getter getRatePerBlockPence().
 public StandardParcel(long id,String trackingCode,int weightGrams,int ratePerBlockPence){
  super(id,trackingCode,weightGrams);
  // TODO Task 1: reject weightGrams <= 0 or ratePerBlockPence <= 0 (IllegalArgumentException), then store the rate.
 }
 @Override public int pricePence(){
  // TODO Task 1: charge ratePerBlockPence for every started 500 g block; reject a price that does not fit in an int.
  throw new UnsupportedOperationException("Task 1");
 }
 @Override public int priority(){ // TODO Task 1: a standard parcel always has priority 1.
  throw new UnsupportedOperationException("Task 1");}
}
