package edu.practice.d11.error;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String room) {
        super("Room not found: " + room);
    }
}
