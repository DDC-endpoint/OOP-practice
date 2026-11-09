package edu.practice.d06.service;

import edu.practice.d06.dto.RoomQuote;
import edu.practice.d06.dto.StayRequest;
import edu.practice.d06.model.Room;
import edu.practice.d06.repo.RoomRepository;

import java.util.Comparator;
import java.util.List;

/** Turns hotel rooms (model) into price quotes (DTOs). */
public class QuoteService {

    private final RoomRepository repo;

    public QuoteService(RoomRepository repo) {
        this.repo = repo;
    }

    /** The quote for one room. Unknown id -> IllegalArgumentException; nights <= 0 -> IllegalArgumentException. */
    public RoomQuote quote(long roomId, int nights) {
        Room room = repo.get(roomId);
        if (room == null) throw new IllegalArgumentException("no room with id " + roomId);
        return toQuote(room, nights);
    }

    /**
     * A quote for every room that can sleep `request.guests()` people, for `request.nights()` nights,
     * cheapest first; equal totals -> lower room id first.
     */
    public List<RoomQuote> quotes(StayRequest request) {
        return repo.all().stream()
                .filter(room -> room.getCapacity() >= request.guests())
                .map(room -> toQuote(room, request.nights()))
                .sorted(Comparator.comparingInt(RoomQuote::totalCents)
                        .thenComparingLong(RoomQuote::roomId))
                .toList();
    }

    /** Model -> DTO in ONE place, so both public methods build quotes the same way. */
    private RoomQuote toQuote(Room room, int nights) {
        return new RoomQuote(room.getId(), room.getNumber(), room.type(), nights, room.priceCents(nights));
    }
}
