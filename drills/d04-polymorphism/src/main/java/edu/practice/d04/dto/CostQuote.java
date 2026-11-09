package edu.practice.d04.dto;

/**
 * Given. What the API would send back for one vehicle: "this vehicle, this far, costs this much".
 * A record: Java writes the constructor, the accessors (vehicleId(), plate(), ...), equals and toString for you.
 */
public record CostQuote(long vehicleId, String plate, String kind, int km, int costCents) { }
