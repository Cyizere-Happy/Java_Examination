package org.rndl.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task 2 – Encapsulation
 * Represents a registered library member.
 * Holds a private list of currently borrowed books (Java Collections – Task 3).
 */
public class Member {
    private int         memberId;
    private String      name;
    private List<Book>  borrowedBooks;   // Task 3 – Java Collections

    public Member(int memberId, String name) {
        this.memberId     = memberId;
        this.name         = name;
        this.borrowedBooks = new ArrayList<>();
    }


    public int         getMemberId()     { return memberId; }
    public String      getName()         { return name; }

    /**
     * Returns an unmodifiable view so callers cannot bypass the borrow limit.
     * Use addBorrowedBook() / removeBorrowedBook() to mutate the list.
     */
    public List<Book>  getBorrowedBooks() {
        return Collections.unmodifiableList(borrowedBooks);
    }

    public void setName(String name) { this.name = name; }

    /**
     * Adds a book to the member's borrowed list.
     * The borrow-limit check happens in LibraryService before this is called.
     */
    public void addBorrowedBook(Book book) {
        borrowedBooks.add(book);
    }

    /**
     * Removes a book from the member's borrowed list when returned.
     */
    public void removeBorrowedBook(Book book) {
        borrowedBooks.remove(book);
    }

    /** Convenience method used by the service layer for the limit check. */
    public int getBorrowedCount() {
        return borrowedBooks.size();
    }

    @Override
    public String toString() {
        return String.format("Member[id=%d, name=%s, borrowed=%d book(s)]",
                memberId, name, borrowedBooks.size());
    }
}