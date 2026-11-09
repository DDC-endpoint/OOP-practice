package edu.practice.courier.controller;
import edu.practice.courier.service.*;import edu.practice.courier.model.*;import edu.practice.courier.dto.*;
import java.util.*;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/parcels") public class ParcelController {
 private final ParcelService service;
 public ParcelController(ParcelService service){this.service=service;}
 @GetMapping public Collection<Parcel> list(){return service.all();}
 // TODO Task 4: GET /{id}/quote, POST /standard (HTTP 201 with the created parcel) and
 // GET /van?capacityGrams=... where capacityGrams defaults to 2000. Delegate all logic to the service.
}
