package edu.practice.d08.repo;

import edu.practice.d08.model.Ticket;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory storage, the same shape as the LBP starters' repositories. Given. */
@Repository
public class TicketRepository {

    private final Map<Long, Ticket> tickets = new ConcurrentHashMap<>();

    public TicketRepository() {
        tickets.put(1L, new Ticket(1, "Jazz Night", "A1", 2_500));
        tickets.put(2L, new Ticket(2, "Jazz Night", "A2", 2_500));
        Ticket taken = new Ticket(3, "Comedy Club", "B7", 1_800);
        taken.bookFor("Kofi");
        tickets.put(3L, taken);
    }

    public Collection<Ticket> all() { return List.copyOf(tickets.values()); }

    public Ticket get(long id) { return tickets.get(id); }

    public boolean contains(long id) { return tickets.containsKey(id); }

    public void save(Ticket ticket) { tickets.put(ticket.getId(), ticket); }
}
