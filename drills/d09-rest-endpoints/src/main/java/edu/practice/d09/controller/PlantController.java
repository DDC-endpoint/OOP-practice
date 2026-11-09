package edu.practice.d09.controller;

import edu.practice.d09.dto.NewPlant;
import edu.practice.d09.dto.PriceQuote;
import edu.practice.d09.model.Plant;
import edu.practice.d09.service.PlantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService service;

    public PlantController(PlantService service) {
        this.service = service;
    }

    @GetMapping
    public List<Plant> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Plant get(@PathVariable long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/price")
    public PriceQuote price(@PathVariable long id, @RequestParam int qty) {
        return service.price(id, qty);
    }

    @GetMapping("/search")
    public List<Plant> search(@RequestParam int maxPriceCents,
                              @RequestParam(defaultValue = "false") boolean inStockOnly) {
        return service.search(maxPriceCents, inStockOnly);
    }

    @PostMapping
    public ResponseEntity<Plant> add(@RequestBody NewPlant body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
    }
}
