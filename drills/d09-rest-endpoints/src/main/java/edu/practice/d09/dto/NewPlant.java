package edu.practice.d09.dto;

/** POST /api/plants body, e.g. {"id":6,"name":"Rosemary","priceCents":399,"stock":7}. */
public record NewPlant(long id, String name, int priceCents, int stock) { }
