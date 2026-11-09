package edu.practice.d01.model;

public class Book extends LibraryItem {

    private final String author;
    private final int pages;

    public Book(long id, String title, String author, int pages) {
        super(id, title);
        if (author == null || author.isBlank()) throw new IllegalArgumentException("author is required");
        if (pages <= 0) throw new IllegalArgumentException("pages must be positive");
        this.author = author.trim();
        this.pages = pages;
    }

    public String getAuthor() { return author; }

    public int getPages() { return pages; }

    @Override
    public int loanDays() {
        return pages > 500 ? 21 : 14;
    }
}
