package edu.practice.d10.dto;

/** POST /api/screenings body, e.g. {"id":7,"film":"Quiet Storm","runtimeMinutes":133,"basePriceCents":1000}. */
public record NewScreening(long id, String film, int runtimeMinutes, int basePriceCents) { }
