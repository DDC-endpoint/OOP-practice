package edu.practice.d06.repo;

import edu.practice.d06.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory storage, the same shape as the LBP starters' repositories. Given. */
public class RoomRepository {

    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();

    public RoomRepository() {
        rooms.put(1L, new StandardRoom(1, "101", 2, 9_000));
        rooms.put(2L, new StandardRoom(2, "102", 4, 12_500));
        rooms.put(3L, new Suite(3, "301", 4, 11_000, 4_000));
        rooms.put(4L, new Suite(4, "302", 6, 15_000, 4_000));
        rooms.put(5L, new StandardRoom(5, "103", 1, 6_500));
    }

    /** A copy of every room. The order is NOT guaranteed: sort it yourself if order matters. */
    public Collection<Room> all() { return List.copyOf(rooms.values()); }

    public Room get(long id) { return rooms.get(id); }

    public boolean contains(long id) { return rooms.containsKey(id); }

    public void save(Room room) { rooms.put(room.getId(), room); }
}
