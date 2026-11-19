package edu.practice.courier.service;
import edu.practice.courier.model.*;import edu.practice.courier.repository.*;import edu.practice.courier.exception.*;
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
  if(body.getId()<=0) throw new IllegalArgumentException("id must be positive");
  String code=body.getTrackingCode()==null?"":body.getTrackingCode().trim();
  if(code.isEmpty()) throw new IllegalArgumentException("trackingCode must not be blank");
  if(body.getWeightGrams()<1||body.getWeightGrams()>30_000) throw new IllegalArgumentException("weightGrams must be 1-30000");
  if(body.getRatePerBlockPence()<1||body.getRatePerBlockPence()>10_000) throw new IllegalArgumentException("ratePerBlockPence must be 1-10000");
  // Uniqueness is checked only after the fields are valid: 400 for bad input, 409 for a clash.
  if(repo.exists(body.getId())) throw new DuplicateParcelException("Parcel ID already exists: "+body.getId());
  if(repo.all().stream().anyMatch(p->p.getTrackingCode().equalsIgnoreCase(code)))
   throw new DuplicateParcelException("Tracking code already exists: "+code);
  Parcel created=new StandardParcel(body.getId(),code,body.getWeightGrams(),body.getRatePerBlockPence());
  repo.save(created);
  return created;
 }
 public VanLoad loadVan(int capacityGrams){
  if(capacityGrams<=0) throw new IllegalArgumentException("capacityGrams must be positive");
  // Polymorphism: toQuote calls pricePence()/priority() through the Parcel reference; each subclass
  // supplies its own rule, so no instanceof is needed. Quotes are built once, then sorted.
  List<Quote> order=repo.all().stream().map(this::toQuote)
   .sorted(Comparator.comparingInt(Quote::getPriority).reversed()   // reversed() applies to priority only
    .thenComparingInt(Quote::getPricePence)
    .thenComparingLong(Quote::getId))
   .toList();
  int remaining=capacityGrams,total=0;
  List<Quote> loaded=new ArrayList<>();List<Long> skipped=new ArrayList<>();
  for(Quote q:order){
   if(q.getWeightGrams()<=remaining){loaded.add(q);remaining-=q.getWeightGrams();total=Math.addExact(total,q.getPricePence());}
   else skipped.add(q.getId());   // greedy: keep going, a lighter parcel later may still fit
  }
  return new VanLoad(capacityGrams,capacityGrams-remaining,remaining,total,loaded,skipped);
 }
 private Quote toQuote(Parcel p){
  return new Quote(p.getId(),p.getTrackingCode(),p.getWeightGrams(),p.priority(),p.pricePence());
 }
}
