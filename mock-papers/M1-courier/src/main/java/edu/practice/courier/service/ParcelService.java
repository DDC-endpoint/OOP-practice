package edu.practice.courier.service;
import edu.practice.courier.model.*;import edu.practice.courier.repository.*;import edu.practice.courier.exception.*;
import org.springframework.stereotype.Service;import java.util.*;
@Service public class ParcelService {
 private final ParcelRepository repo;
 public ParcelService(ParcelRepository repo){this.repo=repo;}
 public Collection<Parcel> all(){return repo.all();}
 public Quote quote(long id){
  // TODO Task 3: find the parcel or throw ParcelNotFoundException; build the Quote from pricePence() and priority().
  throw new UnsupportedOperationException("Task 3");
 }
 public Parcel addStandard(NewStandardParcel body){
  // TODO Task 3: validate ID, tracking code, weight and rate (IllegalArgumentException); trim the tracking code;
  // a duplicate ID or tracking code -> DuplicateParcelException; save and return the new StandardParcel.
  throw new UnsupportedOperationException("Task 3");
 }
 public VanLoad loadVan(int capacityGrams){
  // TODO Task 2: sort priority DESC, pricePence ASC, ID ASC; load each parcel whose weight still fits;
  // record skipped IDs in the order considered. No concrete type checks. (Task 3: reject capacityGrams <= 0.)
  throw new UnsupportedOperationException("Task 2");
 }
 // You may add a private helper method for building Quote objects.
}
