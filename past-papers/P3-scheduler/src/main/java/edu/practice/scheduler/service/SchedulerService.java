package edu.practice.scheduler.service;
import edu.practice.scheduler.model.*;import edu.practice.scheduler.dto.*;import edu.practice.scheduler.repo.*;import edu.practice.scheduler.error.*;
import org.springframework.stereotype.Service;import java.util.*;
@Service public class SchedulerService {
 static final String RELEASE_OUTSIDE_WINDOW="RELEASE_OUTSIDE_WINDOW";
 static final String DEADLINE="DEADLINE";
 static final String EXCEEDS_CAPACITY="EXCEEDS_CAPACITY";
 static final String NO_SLOT="NO_SLOT";

 private final WorkloadRepository repo;
 /** Confirmed (committed) reservations. Only commit() adds to this list. */
 private final List<Reservation> confirmed=new ArrayList<>();
 /** runKey -> the request and the plan it produced, for idempotent retries. */
 private final Map<String,CommitRecord> commits=new HashMap<>();

 private record CommitRecord(CommitRequest request,SchedulePlan plan){}

 public SchedulerService(WorkloadRepository repo){this.repo=repo;}
 public Collection<Workload> all(){return repo.all();}

 public int urgency(long id,int nowTick){
  Workload w=repo.find(id);
  if(w==null) throw new WorkloadNotFoundException(id);
  return w.urgencyAt(nowTick);
 }

 /** Urgency at nowTick DESC, then ID ASC. urgencyAt is called through the Workload reference. */
 public List<Workload> ranked(int nowTick){
  Map<Long,Integer> urgency=new HashMap<>();
  for(Workload w:repo.all()) urgency.put(w.getId(),w.urgencyAt(nowTick));   // compute each urgency once
  return repo.all().stream()
    .sorted(Comparator.comparing((Workload w)->urgency.get(w.getId())).reversed()
      .thenComparingLong(Workload::getId))
    .toList();
 }

 /** Confirmed reservations in a fixed order: startTick, then ID. */
 public synchronized List<Reservation> reservations(){
  return confirmed.stream()
    .sorted(Comparator.comparingInt(Reservation::startTick).thenComparingLong(Reservation::id))
    .toList();
 }

 /** Dry run: same algorithm as commit, but nothing is stored. */
 public synchronized SchedulePlan preview(PlanRequest request){
  validate(request);
  return schedule(request);
 }

 public synchronized SchedulePlan commit(CommitRequest request){
  if(request==null) throw new IllegalArgumentException("request body is required");
  if(request.runKey()==null||request.runKey().isBlank()) throw new IllegalArgumentException("runKey must not be blank");
  PlanRequest planRequest=request.asPlanRequest();
  validate(planRequest);
  CommitRecord previous=commits.get(request.runKey());
  if(previous!=null){
   // Idempotency: the same runKey + same request returns the ORIGINAL plan (not a recomputed one).
   if(previous.request().equals(request)) return previous.plan();
   throw new CommitConflictException(request.runKey());
  }
  SchedulePlan plan=schedule(planRequest);
  confirmed.addAll(plan.selected());   // atomic: the whole method holds the lock
  commits.put(request.runKey(),new CommitRecord(request,plan));
  return plan;
 }

 private static void validate(PlanRequest r){
  if(r==null) throw new IllegalArgumentException("request body is required");
  requireRange("nowTick",r.nowTick(),0,1_000_000);
  requireRange("horizon",r.horizon(),1,24);
  requireRange("cpuCapacity",r.cpuCapacity(),1,64);
  requireRange("memoryCapacity",r.memoryCapacity(),1,1_000_000);
 }

 private static void requireRange(String name,int value,int min,int max){
  if(value<min||value>max) throw new IllegalArgumentException(name+" must be between "+min+" and "+max);
 }

 /**
  * Deterministic earliest-fit scheduling inside the window [nowTick, nowTick + horizon).
  *
  * cpuUsed[i] / memUsed[i] hold what is already booked at tick nowTick + i: first the confirmed
  * reservations, then each assignment this run selects. Workloads are processed in ranked order:
  *  - EXCEEDS_CAPACITY        it needs more CPU or memory than the whole cluster has;
  *  - RELEASE_OUTSIDE_WINDOW  even its earliest start (max(nowTick, release)) can't finish inside the window;
  *  - DEADLINE                its latest legal start is before its earliest start;
  *  - otherwise try every start from earliest to latest = min(window end - duration, latestStartTick());
  *    a start fits only if EVERY tick [start, start + duration) has room for both CPU and memory;
  *  - NO_SLOT                 no start in that range fits.
  * Intervals are half-open, so a job ending at tick 5 and one starting at tick 5 never overlap.
  */
 private SchedulePlan schedule(PlanRequest r){
  int now=r.nowTick();
  int windowEnd=now+r.horizon();   // exclusive; at most 1,000,024, so no overflow
  long[] cpuUsed=new long[r.horizon()];
  long[] memUsed=new long[r.horizon()];
  for(Reservation res:confirmed) book(cpuUsed,memUsed,now,windowEnd,res);

  List<Reservation> selected=new ArrayList<>();
  List<Rejection> rejected=new ArrayList<>();
  for(Workload w:ranked(now)){
   int duration=w.getDurationTicks();
   int earliest=Math.max(now,w.getReleaseTick());
   long windowLatest=(long)windowEnd-duration;
   String reason=null;
   if(w.getCpuCores()>r.cpuCapacity()||w.getMemoryMb()>r.memoryCapacity()){
    reason=EXCEEDS_CAPACITY;
   }else if(earliest>windowLatest){
    reason=RELEASE_OUTSIDE_WINDOW;
   }else if(w.latestStartTick()<earliest){
    reason=DEADLINE;
   }else{
    int latest=(int)Math.min(windowLatest,w.latestStartTick());
    int start=firstFit(w,earliest,latest,now,cpuUsed,memUsed,r);
    if(start<0){
     reason=NO_SLOT;
    }else{
     Reservation res=new Reservation(w.getId(),start,start+duration,w.getCpuCores(),w.getMemoryMb());
     book(cpuUsed,memUsed,now,windowEnd,res);   // later workloads in this run must see it
     selected.add(res);
    }
   }
   if(reason!=null) rejected.add(new Rejection(w.getId(),reason));
  }
  return new SchedulePlan(now,r.horizon(),List.copyOf(selected),List.copyOf(rejected));
 }

 /** The first start in [earliest, latest] where every occupied tick has room, or -1. */
 private static int firstFit(Workload w,int earliest,int latest,int now,long[] cpuUsed,long[] memUsed,PlanRequest r){
  for(int start=earliest;start<=latest;start++){
   boolean fits=true;
   for(int t=start;t<start+w.getDurationTicks()&&fits;t++){
    int i=t-now;
    fits=cpuUsed[i]+w.getCpuCores()<=r.cpuCapacity()&&memUsed[i]+w.getMemoryMb()<=r.memoryCapacity();
   }
   if(fits) return start;
  }
  return -1;
 }

 /** Adds a reservation's load to the ticks it covers inside the window. */
 private static void book(long[] cpuUsed,long[] memUsed,int now,int windowEnd,Reservation res){
  for(int t=Math.max(now,res.startTick());t<Math.min(windowEnd,res.endTickExclusive());t++){
   cpuUsed[t-now]+=res.cpuCores();
   memUsed[t-now]+=res.memoryMb();
  }
 }
}
