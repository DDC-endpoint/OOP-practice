package edu.practice.dispatch.service;
import edu.practice.dispatch.model.*;import edu.practice.dispatch.dto.*;import edu.practice.dispatch.repo.*;import edu.practice.dispatch.error.*;
import org.springframework.stereotype.Service;import java.util.*;
@Service public class DispatchService {
 private static final int URGENT_PRIORITY=3;
 private static final int MAX_VALUE=10_000;

 private final MissionRepository repo;
 public DispatchService(MissionRepository repo){this.repo=repo;}
 public Collection<DispatchMission> all(){return repo.all();}

 public int energy(long id){
  DispatchMission mission=repo.find(id);
  if(mission==null) throw new MissionNotFoundException(id);
  return mission.energyUnits();
 }

 public synchronized DispatchMission addAir(NewAirMission request){
  if(request==null) throw new IllegalArgumentException("request body is required");
  if(request.id()<=0) throw new IllegalArgumentException("id must be positive");
  if(repo.exists(request.id())) throw new IllegalArgumentException("mission "+request.id()+" already exists");
  requireRange("distanceKm",request.distanceKm(),1);
  requireRange("loadKg",request.loadKg(),0);
  requireRange("windPenalty",request.windPenalty(),0);
  AirMission mission=new AirMission(request.id(),request.distanceKm(),request.loadKg(),request.windPenalty(),request.critical());
  mission.energyUnits();   // calculate before saving: an overflow throws here, so nothing is stored
  repo.save(mission);
  return mission;
 }

 /**
  * Greedy plan:
  *  1. keep every mission, or only priority >= 3 when urgentOnly (the others are not planned at all);
  *  2. sort by priority DESC, then energy ASC, then ID ASC, so the result is deterministic;
  *  3. walk that list once: take a mission if its energy fits what is left, otherwise record it
  *     as skipped and carry on, because a later, cheaper mission may still fit.
  * energyUnits() and priority() are called through the DispatchMission reference: no type checks.
  */
 public DispatchPlan plan(int capacity,boolean urgentOnly){
  if(capacity<=0) throw new IllegalArgumentException("capacity must be positive");
  List<DispatchMission> candidates=repo.all().stream()
    .filter(m->!urgentOnly||m.priority()>=URGENT_PRIORITY)
    .sorted(Comparator.comparingInt(DispatchMission::priority).reversed()
      .thenComparingInt(DispatchMission::energyUnits)
      .thenComparingLong(DispatchMission::getId))
    .toList();
  int remaining=capacity;
  List<PlanLine> selected=new ArrayList<>();
  List<Long> skipped=new ArrayList<>();
  for(DispatchMission m:candidates){
   int energy=m.energyUnits();
   if(energy<=remaining){
    // getSimpleName() is only a display label for the DTO, not a type check.
    selected.add(new PlanLine(m.getId(),m.getClass().getSimpleName(),m.priority(),energy));
    remaining-=energy;
   }else{
    skipped.add(m.getId());
   }
  }
  return new DispatchPlan(capacity,capacity-remaining,remaining,selected,skipped);
 }

 private static void requireRange(String name,int value,int min){
  if(value<min||value>MAX_VALUE) throw new IllegalArgumentException(name+" must be between "+min+" and "+MAX_VALUE);
 }
}
