package edu.practice.d07.service;

import edu.practice.d07.dto.NewCourse;
import edu.practice.d07.error.CourseNotFoundException;
import edu.practice.d07.model.Course;
import edu.practice.d07.repo.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/** The business rules for courses. Controllers (d09) will call these methods and do nothing else. */
@Service
public class CourseService {

    /** The biggest lecture theatre holds 300 students. */
    public static final int MAX_CAPACITY = 300;

    private final CourseRepository repo;

    public CourseService(CourseRepository repo) {   // constructor injection: Spring passes the repository in
        this.repo = repo;
    }

    /** Given: every course, lowest id first. */
    public List<Course> all() {
        return repo.all().stream().sorted(Comparator.comparingLong(Course::getId)).toList();
    }

    /** The course with this id. */
    public Course get(long id) {
        Course course = repo.get(id);
        if (course == null) {
            throw new CourseNotFoundException(id);
        }
        return course;
    }

    /** Checks the new course, stores it, and returns the stored course. */
    public Course add(NewCourse body) {
        if (body == null) {
            throw new IllegalArgumentException("course details are required");
        }
        if (body.id() <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        if (body.code() == null || body.code().isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (body.title() == null || body.title().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (body.capacity() <= 0 || body.capacity() > MAX_CAPACITY) {
            throw new IllegalArgumentException("capacity must be between 1 and " + MAX_CAPACITY);
        }
        if (repo.contains(body.id())) {
            throw new IllegalArgumentException("a course with id " + body.id() + " already exists");
        }
        Course course = new Course(body.id(), body.code().trim(), body.title().trim(), body.capacity());
        repo.save(course);
        return course;
    }
}
