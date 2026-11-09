package edu.practice.d07.dto;

/**
 * What a client sends when it wants to add a course (later this arrives as JSON in a POST body).
 * A record: Java writes the constructor, the accessors id(), code(), title(), capacity(),
 * equals and toString for you. Nothing in here is checked yet: it is raw, untrusted input.
 */
public record NewCourse(long id, String code, String title, int capacity) { }
