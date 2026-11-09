package edu.practice.courier.model;
public class StandardParcel extends Parcel {
 private final int ratePerBlockPence;
 public StandardParcel(long id,String trackingCode,int weightGrams,int ratePerBlockPence){
  super(id,trackingCode,weightGrams); // super(...) must be the first statement, so validate straight after it
  if(weightGrams<=0) throw new IllegalArgumentException("weightGrams must be positive");
  if(ratePerBlockPence<=0) throw new IllegalArgumentException("ratePerBlockPence must be positive");
  this.ratePerBlockPence=ratePerBlockPence;
 }
 public int getRatePerBlockPence(){return ratePerBlockPence;}
 @Override public int pricePence(){
  int w=getWeightGrams();
  // Ceiling division: one block per STARTED 500 g. (w+499)/500 would overflow near Integer.MAX_VALUE,
  // so add the extra block only when there is a remainder.
  long blocks=w/500+(w%500==0?0:1);
  long price=blocks*ratePerBlockPence;          // long maths: cannot overflow here
  if(price>Integer.MAX_VALUE) throw new IllegalArgumentException("price does not fit in an int");
  return (int)price;
 }
 @Override public int priority(){return 1;}
}
