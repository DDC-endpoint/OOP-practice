package edu.practice.courier.model;
/** Response: one parcel's price and priority. A plain class: Jackson turns the getters into JSON fields. */
public class Quote {
 private final long id; private final String trackingCode; private final int weightGrams; private final int priority; private final int pricePence;
 public Quote(long id,String trackingCode,int weightGrams,int priority,int pricePence){
  this.id=id;this.trackingCode=trackingCode;this.weightGrams=weightGrams;this.priority=priority;this.pricePence=pricePence;}
 public long getId(){return id;}
 public String getTrackingCode(){return trackingCode;}
 public int getWeightGrams(){return weightGrams;}
 public int getPriority(){return priority;}
 public int getPricePence(){return pricePence;}
}
