package edu.practice.d07;

import edu.practice.d07.dto.NewCourse;
import edu.practice.d07.error.CourseNotFoundException;
import edu.practice.d07.model.Course;
import edu.practice.d07.repo.CourseRepository;
import edu.practice.d07.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'CourseServiceTest'.
 *
 * No Spring here: each test builds a fresh repository and service with `new`,
 * exactly like Spring would (it calls the same constructor). So every test starts
 * with the two seeded courses and nothing else, whatever order the tests run in.
 */
class CourseServiceTest {

    private CourseRepository repo;
    private CourseService service;

    @BeforeEach
    void freshServiceForEveryTest() {
        repo = new CourseRepository();
        service = new CourseService(repo);
    }

    /** add(...) must throw IllegalArgumentException with a useful message, and must NOT store anything. */
    private void assertRejected(NewCourse body, String why) {
        int before = repo.all().size();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.add(body),
                "add(" + body + ") should throw IllegalArgumentException: " + why);
        assertNotNull(e.getMessage(), "give the exception a message, e.g. new IllegalArgumentException(\"title is required\")");
        assertFalse(e.getMessage().isBlank(), "the exception message must say what was wrong (it becomes the API error message)");
        assertEquals(before, repo.all().size(), "a rejected course must NOT be saved: " + body);
    }

    @Test
    @DisplayName("1. get(id) returns the stored course")
    void getReturnsStoredCourse() {
        Course oop = service.get(1);
        assertEquals(1L, oop.getId());
        assertEquals("OOP101", oop.getCode());
        assertEquals("Object Oriented Programming", oop.getTitle());
        assertEquals(120, oop.getCapacity());

        Course web = service.get(2);
        assertEquals("WEB205", web.getCode());
        assertEquals(35, web.getCapacity());
        assertSame(repo.get(2), web, "get should return the course from the repository, not build a new one");
    }

    @Test
    @DisplayName("2. get(id) throws CourseNotFoundException for an unknown id (never returns null)")
    void getUnknownThrowsNotFound() {
        for (long id : new long[]{3, 99, 0, -4}) {
            CourseNotFoundException e = assertThrows(CourseNotFoundException.class, () -> service.get(id),
                    "get(" + id + ") should throw CourseNotFoundException");
            assertTrue(e.getMessage().contains(String.valueOf(id)), "use new CourseNotFoundException(id) so the message names the id");
        }
    }

    @Test
    @DisplayName("3. add(...) stores a valid course and returns it")
    void addValidCourse() {
        Course c = service.add(new NewCourse(3, "ALG110", "Algorithms", 60));
        assertEquals(3L, c.getId());
        assertEquals("ALG110", c.getCode());
        assertEquals("Algorithms", c.getTitle());
        assertEquals(60, c.getCapacity());
        assertSame(c, repo.get(3), "the course you return should be the one you saved");

        service.add(new NewCourse(42, "NET330", "Networks", 18));
        assertEquals(4, repo.all().size());
        assertEquals("Networks", service.get(42).getTitle(), "a course you added must be found by get(id) afterwards");
    }

    @Test
    @DisplayName("4. add(...) trims code and title BEFORE storing them")
    void addTrims() {
        Course c = service.add(new NewCourse(7, "  SEC240 ", "  Cyber Security\t", 45));
        assertEquals("SEC240", c.getCode());
        assertEquals("Cyber Security", c.getTitle());
        assertEquals("SEC240", repo.get(7).getCode(), "the STORED code must be trimmed too");
        assertEquals("Cyber Security", repo.get(7).getTitle(), "the STORED title must be trimmed too");

        service.add(new NewCourse(8, "\tAI300", "Machine Learning   ", 90));
        assertEquals("AI300", repo.get(8).getCode());
        assertEquals("Machine Learning", repo.get(8).getTitle());
    }

    @Test
    @DisplayName("5. add(...) rejects an id that is zero or negative")
    void rejectsBadId() {
        assertRejected(new NewCourse(0, "ART100", "Drawing", 20), "id 0 is not positive");
        assertRejected(new NewCourse(-12, "ART100", "Drawing", 20), "a negative id");
    }

    @Test
    @DisplayName("6. add(...) rejects a null or blank code")
    void rejectsBadCode() {
        assertRejected(new NewCourse(5, null, "Drawing", 20), "code is null");
        assertRejected(new NewCourse(5, "", "Drawing", 20), "code is empty");
        assertRejected(new NewCourse(5, "   ", "Drawing", 20), "code is only spaces");
        assertFalse(repo.contains(5));
    }

    @Test
    @DisplayName("7. add(...) rejects a null or blank title")
    void rejectsBadTitle() {
        assertRejected(new NewCourse(6, "ART100", null, 20), "title is null");
        assertRejected(new NewCourse(6, "ART100", "\t \n", 20), "title is only whitespace");
        assertFalse(repo.contains(6));
    }

    @Test
    @DisplayName("8. add(...) needs a capacity from 1 to 300 (MAX_CAPACITY)")
    void capacityRange() {
        assertRejected(new NewCourse(9, "BIO101", "Biology", 0), "capacity 0");
        assertRejected(new NewCourse(9, "BIO101", "Biology", -30), "a negative capacity");
        assertRejected(new NewCourse(9, "BIO101", "Biology", 301), "capacity 301 is over MAX_CAPACITY (300)");
        assertRejected(new NewCourse(9, "BIO101", "Biology", 5_000), "capacity 5000 is over MAX_CAPACITY (300)");

        assertEquals(300, service.add(new NewCourse(9, "BIO101", "Biology", 300)).getCapacity(), "exactly 300 is allowed");
        assertEquals(1, service.add(new NewCourse(10, "CHE101", "Chemistry", 1)).getCapacity(), "exactly 1 is allowed");
    }

    @Test
    @DisplayName("9. add(...) rejects a duplicate id and leaves the existing course untouched")
    void duplicateId() {
        assertRejected(new NewCourse(1, "HIJACK", "Not OOP", 10), "id 1 is already used by OOP101");
        assertEquals("OOP101", repo.get(1).getCode(), "the old course must NOT be replaced (repo.save overwrites!)");
        assertEquals(120, repo.get(1).getCapacity());

        service.add(new NewCourse(15, "MUS120", "Music Theory", 25));
        assertRejected(new NewCourse(15, "MUS999", "Another Music", 50), "id 15 was added a moment ago");
        assertEquals("Music Theory", repo.get(15).getTitle(), "the first course with id 15 must survive");
    }
}
