package edu.practice.d10.service;

import edu.practice.d10.dto.NewScreening;
import edu.practice.d10.dto.Quote;
import edu.practice.d10.error.ScreeningNotFoundException;
import edu.practice.d10.model.Screening;
import edu.practice.d10.repo.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/** The business rules: validation, prices and ordering. */
@Service
public class ScreeningService {

    /** One quote covers 1 to 10 seats. */
    public static final int MAX_SEATS = 10;

    private final ScreeningRepository repo;

    public ScreeningService(ScreeningRepository repo) { this.repo = repo; }

    /** Every screening, lowest id first. */
    public List<Screening> all() {
        return repo.all().stream().sorted(Comparator.comparingLong(Screening::getId)).toList();
    }

    /** One screening, or ScreeningNotFoundException (404). */
    public Screening get(long id) {
        Screening screening = repo.get(id);
        if (screening == null) throw new ScreeningNotFoundException(id);
        return screening;
    }

    /** The price of `seats` seats at one screening. */
    public Quote quote(long id, int seats) {
        checkSeats(seats);
        return toQuote(get(id), seats);
    }

    /** A quote for every screening: most expensive first, and equal totals by lowest id first. */
    public List<Quote> quotes(int seats) {
        checkSeats(seats);
        return repo.all().stream()
                .map(s -> toQuote(s, seats))
                .sorted(Comparator.comparingInt(Quote::totalCents).thenComparingLong(Quote::screeningId).reversed())
                .toList();
    }

    /** Validates, stores and returns a new screening. Bad input -> IllegalArgumentException (400), nothing stored. */
    public Screening add(NewScreening body) {
        if (body == null) throw new IllegalArgumentException("screening details are required");
        if (body.id() <= 0) throw new IllegalArgumentException("id must be positive");
        if (body.film() == null || body.film().isBlank()) throw new IllegalArgumentException("film is required");
        if (body.runtimeMinutes() <= 0) throw new IllegalArgumentException("runtimeMinutes must be positive");
        if (body.basePriceCents() <= 0) throw new IllegalArgumentException("basePriceCents must be positive");
        if (repo.contains(body.id())) throw new IllegalArgumentException("screening " + body.id() + " already exists");
        Screening screening = new Screening(body.id(), body.film().trim(), body.runtimeMinutes(), body.basePriceCents());
        repo.save(screening);
        return screening;
    }

    private static void checkSeats(int seats) {
        if (seats < 1 || seats > MAX_SEATS) throw new IllegalArgumentException("seats must be 1.." + MAX_SEATS);
    }

    private static Quote toQuote(Screening s, int seats) {
        int seatPrice = s.seatPriceCents();
        return new Quote(s.getId(), s.getFilm(), seats, seatPrice, seats * seatPrice);
    }
}
