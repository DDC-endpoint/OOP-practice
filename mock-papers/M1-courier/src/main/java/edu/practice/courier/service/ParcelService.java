package edu.practice.courier.service;
import edu.practice.courier.model.*;import edu.practice.courier.dto.*;import edu.practice.courier.repo.*;import edu.practice.courier.error.*;
import org.springframework.stereotype.Service;import java.util.*;
@Service public class ParcelService {
 private final ParcelRepository repo;
 public ParcelService(ParcelRepository repo){this.repo=repo;}
 public Collection<Parcel> all(){return repo.all();}
 public Quote quote(long id){
  Parcel p=repo.find(id);
  if(p==null) throw new ParcelNotFoundException(id);
  return toQuote(p);
 }
 public Parcel addStandard(NewStandardParcel body){
  if(body==null) throw new IllegalArgumentException("request body is required");
  if(body.id()<=0) throw new IllegalArgumentException("id must be positive");
  String code=body.trackingCode()==null?"":body.trackingCode().trim();
  if(code.isEmpty()) throw new IllegalArgumentException("trackingCode must not be blank");
  if(body.weightGrams()<1||body.weightGrams()>30_000) throw new IllegalArgumentException("weightGrams must be 1-30000");
  if(body.ratePerBlockPence()<1||body.ratePerBlockPence()>10_000) throw new IllegalArgumentException("ratePerBlockPence must be 1-10000");
  // Uniqueness is checked only after the fields are valid: 400 for bad input, 409 for a clash.
  if(repo.exists(body.id())) throw new DuplicateParcelException("Parcel ID already exists: "+body.id());
  if(repo.all().stream().anyMatch(p->p.getTrackingCode().equalsIgnoreCase(code)))
   throw new DuplicateParcelException("Tracking code already exists: "+code);
  Parcel created=new StandardParcel(body.id(),code,body.weightGrams(),body.ratePerBlockPence());
  repo.save(created);
  return created;
 }
 public VanLoad loadVan(int capacityGrams){
  if(capacityGrams<=0) throw new IllegalArgumentException("capacityGrams must be positive");
  // Polymorphism: toQuote calls pricePence()/priority() through the Parcel reference; each subclass
  // supplies its own rule, so no instanceof is needed. Quotes are built once, then sorted.
  List<Quote> order=repo.all().stream().map(this::toQuote)
   .sorted(Comparator.comparingInt(Quote::priority).reversed()   // reversed() applies to priority only
    .thenComparingInt(Quote::pricePence)
    .thenComparingLong(Quote::id))
   .toList();
  int remaining=capacityGrams,total=0;
  List<Quote> loaded=new ArrayList<>();List<Long> skipped=new ArrayList<>();
  for(Quote q:order){
   if(q.weightGrams()<=remaining){loaded.add(q);remaining-=q.weightGrams();total=Math.addExact(total,q.pricePence());}
   else skipped.add(q.id());   // greedy: keep going, a lighter parcel later may still fit
  }
  return new VanLoad(capacityGrams,capacityGrams-remaining,remaining,total,loaded,skipped);
 }
 private Quote toQuote(Parcel p){
  return new Quote(p.getId(),p.getTrackingCode(),p.getWeightGrams(),p.priority(),p.pricePence());
 }
}
