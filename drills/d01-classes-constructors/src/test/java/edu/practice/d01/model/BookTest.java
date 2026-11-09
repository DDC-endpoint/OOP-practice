package edu.practice.d01.model;

import edu.practice.testkit.Reflect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run me: right-click this file in IntelliJ -> Run 'BookTest'.
 * Every test is red at the start. Work through LESSON.md until they are all green.
 */
class BookTest {

    private static Object book(long id, String title, String author, int pages) {
        return new Book(id, title, author, pages);
    }

    @Test
    @DisplayName("1. Book extends LibraryItem")
    void bookIsALibraryItem() {
        assertTrue(Reflect.extendsClass(Book.class, LibraryItem.class),
                "Book must extend LibraryItem (class Book extends LibraryItem)");
    }

    @Test
    @DisplayName("2. author and pages are private final fields")
    void fieldsArePrivateAndFinal() throws Exception {
        for (String name : new String[]{"author", "pages"}) {
            Field f;
            try {
                f = Book.class.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                fail("Book needs a field called " + name);
                return;
            }
            assertTrue(Modifier.isPrivate(f.getModifiers()), name + " should be private");
            assertTrue(Modifier.isFinal(f.getModifiers()), name + " should be final");
        }
        assertEquals(String.class, Book.class.getDeclaredField("author").getType());
        assertEquals(int.class, Book.class.getDeclaredField("pages").getType());
    }

    @Test
    @DisplayName("3. constructor passes id and title up to the parent")
    void constructorChainsToParent() {
        Object b = book(7, "Dune", "Frank Herbert", 612);
        assertEquals(7L, Reflect.call(b, "getId"));
        assertEquals("Dune", Reflect.call(b, "getTitle"));
        Object c = book(42, "  The Hobbit ", "J. R. R. Tolkien", 310);
        assertEquals(42L, Reflect.call(c, "getId"));
        assertEquals("The Hobbit", Reflect.call(c, "getTitle"), "the parent should trim the title");
    }

    @Test
    @DisplayName("4. getters return what was passed in (author trimmed)")
    void gettersReturnValues() {
        Object b = book(7, "Dune", "Frank Herbert", 612);
        assertEquals("Frank Herbert", Reflect.call(b, "getAuthor"));
        assertEquals(612, Reflect.call(b, "getPages"));
        Object c = book(8, "Emma", "  Jane Austen  ", 474);
        assertEquals("Jane Austen", Reflect.call(c, "getAuthor"));
        assertEquals(474, Reflect.call(c, "getPages"));
    }

    @Test
    @DisplayName("5. loanDays: 21 days above 500 pages, otherwise 14")
    void loanDaysRule() {
        assertEquals(14, Reflect.call(book(1, "A", "X", 120), "loanDays"));
        assertEquals(14, Reflect.call(book(2, "B", "X", 500), "loanDays"), "exactly 500 pages is NOT over 500");
        assertEquals(21, Reflect.call(book(3, "C", "X", 501), "loanDays"));
        assertEquals(21, Reflect.call(book(4, "D", "X", 1_200), "loanDays"));
    }

    @Test
    @DisplayName("6. describe() works for a Book (uses your loanDays)")
    void describeUsesLoanDays() {
        assertEquals("Book #7: Dune (21 days)", Reflect.call(book(7, "Dune", "Frank Herbert", 612), "describe"));
        assertEquals("Book #9: Matilda (14 days)", Reflect.call(book(9, "Matilda", "Roald Dahl", 240), "describe"));
    }

    @Test
    @DisplayName("7. Book rejects bad pages and a bad author")
    void bookValidation() {
        assertThrows(IllegalArgumentException.class, () -> book(1, "A", "X", 0));
        assertThrows(IllegalArgumentException.class, () -> book(1, "A", "X", -40));
        assertThrows(IllegalArgumentException.class, () -> book(1, "A", "   ", 100));
        assertThrows(IllegalArgumentException.class, () -> book(1, "A", null, 100));
    }

    @Test
    @DisplayName("8. LibraryItem (the parent) rejects a bad id or title")
    void parentValidation() {
        assertThrows(IllegalArgumentException.class, () -> book(0, "A", "X", 100));
        assertThrows(IllegalArgumentException.class, () -> book(-3, "A", "X", 100));
        assertThrows(IllegalArgumentException.class, () -> book(1, "  ", "X", 100));
        assertThrows(IllegalArgumentException.class, () -> book(1, null, "X", 100));
    }
}
