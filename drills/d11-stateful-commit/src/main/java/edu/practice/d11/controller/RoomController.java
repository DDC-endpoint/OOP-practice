package edu.practice.d11.controller;

import edu.practice.d11.dto.CommitRequest;
import edu.practice.d11.dto.CommitResult;
import edu.practice.d11.dto.Plan;
import edu.practice.d11.dto.PreviewRequest;
import edu.practice.d11.model.Booking;
import edu.practice.d11.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Given and finished: every endpoint is one line that asks the service. */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final BookingService service;

    public RoomController(BookingService service) {
        this.service = service;
    }

    @GetMapping("/bookings")
    public List<Booking> bookings() {
        return service.bookings();
    }

    @PostMapping("/preview")
    public Plan preview(@RequestBody PreviewRequest body) {
        return service.preview(body.bookings());
    }

    @PostMapping("/commit")
    public CommitResult commit(@RequestBody CommitRequest body) {
        return service.commit(body);
    }
}
