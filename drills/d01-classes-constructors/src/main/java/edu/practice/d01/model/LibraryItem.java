package edu.practice.d01.model;

/**
 * The parent of every item the library lends out.
 * It is abstract: you can't create a plain "LibraryItem", only a kind of item (a Book, ...).
 */
public abstract class LibraryItem {

    private final long id;        // private = only this class can touch it
    private final String title;   // final   = set once, in the constructor, never changed

    protected LibraryItem(long id, String title) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is required");
        this.id = id;
        this.title = title.trim();
    }

    public long getId() { return id; }

    public String getTitle() { return title; }

    /** How many days this item may be borrowed for. Each kind of item has its own rule. */
    public abstract int loanDays();

    /** Example: "Book #7: Dune (21 days)". Already done: notice it calls loanDays() without knowing the subclass. */
    public String describe() {
        return getClass().getSimpleName() + " #" + id + ": " + title + " (" + loanDays() + " days)";
    }
}
