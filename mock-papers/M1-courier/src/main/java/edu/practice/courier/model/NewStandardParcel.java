package edu.practice.courier.model;
/** Request body of POST /parcels/standard. A plain class: Jackson uses the no-arg constructor and the setters. */
public class NewStandardParcel {
 private long id; private String trackingCode; private int weightGrams; private int ratePerBlockPence;
 public NewStandardParcel(){}
 public NewStandardParcel(long id,String trackingCode,int weightGrams,int ratePerBlockPence){
  this.id=id;this.trackingCode=trackingCode;this.weightGrams=weightGrams;this.ratePerBlockPence=ratePerBlockPence;}
 public long getId(){return id;}
 public void setId(long id){this.id=id;}
 public String getTrackingCode(){return trackingCode;}
 public void setTrackingCode(String trackingCode){this.trackingCode=trackingCode;}
 public int getWeightGrams(){return weightGrams;}
 public void setWeightGrams(int weightGrams){this.weightGrams=weightGrams;}
 public int getRatePerBlockPence(){return ratePerBlockPence;}
 public void setRatePerBlockPence(int ratePerBlockPence){this.ratePerBlockPence=ratePerBlockPence;}
}
