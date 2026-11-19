package edu.practice.courier.controller;
import edu.practice.courier.service.*;import edu.practice.courier.model.*;
import java.util.*;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/parcels") public class ParcelController {
 private final ParcelService service;
 public ParcelController(ParcelService service){this.service=service;}
 @GetMapping public Collection<Parcel> list(){return service.all();}
 @GetMapping("/{id}/quote") public Quote quote(@PathVariable long id){return service.quote(id);}
 @PostMapping("/standard") public ResponseEntity<Parcel> addStandard(@RequestBody NewStandardParcel body){
  return ResponseEntity.status(HttpStatus.CREATED).body(service.addStandard(body)); // 201, not the default 200
 }
 @GetMapping("/van") public VanLoad van(@RequestParam(defaultValue="2000") int capacityGrams){
  return service.loadVan(capacityGrams);
 }
}
