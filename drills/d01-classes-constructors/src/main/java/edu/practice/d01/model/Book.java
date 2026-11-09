package edu.practice.d01.model;

// TODO d01-1: make Book a kind of LibraryItem (use extends).
//             Once you do, Java will insist you also write loanDays() (TODO d01-5).
public class Book {

    // TODO d01-2: two fields that can never change after construction:
    //             author (String) and pages (int). Keep them private.

    public Book(long id, String title, String author, int pages) {
        // TODO d01-3: the FIRST line must hand id and title to the parent constructor.
        //             Then validate: author not null/blank (store it trimmed), pages > 0.
        //             Bad values -> throw new IllegalArgumentException("...").
        throw new UnsupportedOperationException("d01: write the Book constructor");
    }

    // TODO d01-4: public getters getAuthor() and getPages().

    // TODO d01-5: loanDays(): books over 500 pages may be borrowed for 21 days, all others 14.
}
