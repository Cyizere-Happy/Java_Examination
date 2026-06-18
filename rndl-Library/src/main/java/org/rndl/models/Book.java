package org.rndl.models;

public class Book {

    // Private fields (encapsulation)
    private String  isbn;
    private String  title;
    private String  author;
    private int     publicationYear;
    private boolean available;

    // Constructor
    public Book(String isbn, String title, String author, int publicationYear) {
        this.isbn            = isbn;
        this.title           = title;
        this.author          = author;
        this.publicationYear = publicationYear;
        this.available       = true;   // every new book starts as available
    }

    // Getters
    public String  getIsbn()            { return isbn; }
    public String  getTitle()           { return title; }
    public String  getAuthor()          { return author; }
    public int     getPublicationYear() { return publicationYear; }
    public boolean isAvailable()        { return available; }

    // Setters
    public void setTitle(String title)                   { this.title           = title; }
    public void setAuthor(String author)                 { this.author          = author; }
    public void setPublicationYear(int publicationYear)  { this.publicationYear = publicationYear; }
    public void setAvailable(boolean available)          { this.available       = available; }

    // Utility
    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s (%d) — %s",
                isbn, title, author, publicationYear,
                available ? "Available" : "Borrowed");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return isbn != null ? isbn.equals(book.isbn) : book.isbn == null;
    }

    @Override
    public int hashCode() {
        return isbn != null ? isbn.hashCode() : 0;
    }
}