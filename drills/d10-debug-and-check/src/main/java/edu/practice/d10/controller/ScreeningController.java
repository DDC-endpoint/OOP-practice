package edu.practice.d10.controller;

import edu.practice.d10.dto.NewScreening;
import edu.practice.d10.dto.Quote;
import edu.practice.d10.model.Screening;
import edu.practice.d10.service.ScreeningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService service;

    public ScreeningController(ScreeningService service) {
        this.service = service;
    }

    @GetMapping
    public List<Screening> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Screening get(@PathVariable long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/quote")
    public Quote quote(@PathVariable long id, @RequestParam int seats) {
        return service.quote(id, seats);
    }

    @GetMapping("/quotes")
    public List<Quote> quotes(@RequestParam(defaultValue = "1") int seats) {
        return service.quotes(seats);
    }

    @PostMapping
    public ResponseEntity<Screening> add(@RequestBody NewScreening body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(body));
    }
}
