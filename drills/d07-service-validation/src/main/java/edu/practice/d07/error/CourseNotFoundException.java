package edu.practice.d07.error;

/** Thrown when nobody has a course with this id. In d08 you'll turn it into HTTP 404. */
public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(long id) {
        super("Course not found: " + id);
    }
}
