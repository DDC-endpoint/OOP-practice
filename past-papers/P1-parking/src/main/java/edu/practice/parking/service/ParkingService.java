package edu.practice.parking.service;
import edu.practice.parking.model.*;import edu.practice.parking.dto.*;import edu.practice.parking.repo.*;import edu.practice.parking.error.*;
import org.springframework.stereotype.Service;import java.util.*;
@Service public class ParkingService {
 private final PassRepository repo;
 public ParkingService(PassRepository repo){this.repo=repo;}
 public Collection<ParkingPass> all(){return repo.all();}

 public Quote quote(long id,int minutes){
  requirePositiveMinutes(minutes);
  ParkingPass pass=repo.get(id);
  if(pass==null) throw new PassNotFoundException(id);
  return toQuote(pass,minutes);
 }

 public List<Quote> allQuotes(int minutes){
  requirePositiveMinutes(minutes);
  // priceCents is called through the ParkingPass reference: each subclass applies its own rule.
  return repo.all().stream()
    .map(pass->toQuote(pass,minutes))
    .sorted(Comparator.comparingInt(Quote::priceCents).thenComparingLong(Quote::id))
    .toList();
 }

 public synchronized ParkingPass addHourly(NewHourlyPass body){
  if(body==null) throw new IllegalArgumentException("request body is required");
  if(body.id()<=0) throw new IllegalArgumentException("id must be positive");
  if(repo.contains(body.id())) throw new IllegalArgumentException("pass "+body.id()+" already exists");
  if(body.registration()==null||body.registration().isBlank()) throw new IllegalArgumentException("registration must not be blank");
  if(body.rateCents()<=0) throw new IllegalArgumentException("rateCents must be positive");
  HourlyPass pass=new HourlyPass(body.id(),body.registration().trim(),body.rateCents());
  repo.save(pass);
  return pass;
 }

 private static void requirePositiveMinutes(int minutes){
  if(minutes<=0) throw new IllegalArgumentException("minutes must be positive");
 }

 private static Quote toQuote(ParkingPass pass,int minutes){
  // getSimpleName() is only a display label for the DTO, not a type check.
  return new Quote(pass.getId(),pass.getRegistration(),pass.getClass().getSimpleName(),minutes,pass.priceCents(minutes));
 }
}
