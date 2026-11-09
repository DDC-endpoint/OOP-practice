package edu.practice.courier.model;
public class ExpressParcel extends Parcel {
 private final int basePence; private final boolean signatureRequired;
 public ExpressParcel(long id,String trackingCode,int weightGrams,int basePence,boolean signatureRequired){
  super(id,trackingCode,weightGrams);
  if(weightGrams<=0) throw new IllegalArgumentException("weightGrams must be positive");
  if(basePence<0) throw new IllegalArgumentException("basePence must not be negative");
  this.basePence=basePence;this.signatureRequired=signatureRequired;
 }
 public int getBasePence(){return basePence;}
 public boolean isSignatureRequired(){return signatureRequired;}
 @Override public int pricePence(){
  int w=getWeightGrams();
  long blocks=w/250+(w%250==0?0:1);             // per started 250 g, overflow-safe ceiling
  long price=basePence+90L*blocks+(signatureRequired?300:0);
  if(price>Integer.MAX_VALUE) throw new IllegalArgumentException("price does not fit in an int");
  return (int)price;
 }
 @Override public int priority(){return signatureRequired?3:2;}
}
