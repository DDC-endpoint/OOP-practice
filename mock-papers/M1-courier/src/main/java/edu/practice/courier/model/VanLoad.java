package edu.practice.courier.model;
import java.util.List;
/** Response of GET /parcels/van. A plain class: Jackson turns the getters into JSON fields. */
public class VanLoad {
 private final int capacityGrams; private final int weightUsed; private final int weightRemaining; private final int totalPence;
 private final List<Quote> loaded; private final List<Long> skipped;
 public VanLoad(int capacityGrams,int weightUsed,int weightRemaining,int totalPence,List<Quote> loaded,List<Long> skipped){
  this.capacityGrams=capacityGrams;this.weightUsed=weightUsed;this.weightRemaining=weightRemaining;this.totalPence=totalPence;
  this.loaded=List.copyOf(loaded);this.skipped=List.copyOf(skipped);}
 public int getCapacityGrams(){return capacityGrams;}
 public int getWeightUsed(){return weightUsed;}
 public int getWeightRemaining(){return weightRemaining;}
 public int getTotalPence(){return totalPence;}
 public List<Quote> getLoaded(){return loaded;}
 public List<Long> getSkipped(){return skipped;}
}
