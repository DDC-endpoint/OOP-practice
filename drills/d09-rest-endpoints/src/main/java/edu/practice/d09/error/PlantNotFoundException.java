package edu.practice.d09.error;

public class PlantNotFoundException extends RuntimeException {
    public PlantNotFoundException(long id) {
        super("Plant not found: " + id);
    }
}
