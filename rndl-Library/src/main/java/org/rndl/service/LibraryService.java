package org.rndl.service;

import org.rndl.db.DBConnection;
import org.rndl.models.Book;
import org.rndl.models.Member;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task 2 – Encapsulation  (service / DAO layer)
 * Task 3 – Core Logic & Collections
 * Task 5 – Data Persistence & JDBC
 *
 * LibraryService is the central component of the system.
 * It manages the in-memory book catalog (HashMap), enforces the 5-book
 * borrow limit, and persists every transaction to the database via JDBC.
 *
 * Thread-safety: the public borrow / return methods are synchronized so
 * that BorrowTask threads (Task 4) cannot corrupt shared state.
 */
public class LibraryService {

    private static volatile LibraryService instance;

    private LibraryService() {}

    public static LibraryService getInstance() {
        if (instance == null) {
            synchronized (LibraryService.class) {
                if (instance == null) {
                    instance = new LibraryService();
                }
            }
        }
        return instance;
    }

    private final Map<String, Book>    bookCatalog    = new HashMap<>();
    private final Map<Integer, Member> memberRegistry = new HashMap<>();

    private static final int MAX_BORROW_LIMIT = 5;


    public void addBook(Book book) {
        bookCatalog.put(book.getIsbn(), book);
    }

    public void addMember(Member member) {
        memberRegistry.put(member.getMemberId(), member);
    }

    public List<Book> listAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : bookCatalog.values()) {
            if (book.isAvailable()) {
                available.add(book);
            }
        }
        return available;
    }

    // ── Task 3 + Task 5: Borrow a book ───────────────────────────────────────
    public synchronized boolean borrowBook(int memberId, String isbn) {

        // ── Validate member ───────────────────────────────────────────────
        Member member = memberRegistry.get(memberId);
        if (member == null) {
            System.out.println("  [ERROR] Member ID " + memberId + " not found.");
            return false;
        }

        // ── Task 3: enforce borrow limit ─────────────────────────────────
        if (member.getBorrowedCount() >= MAX_BORROW_LIMIT) {
            System.out.println("  [DENIED] " + member.getName()
                    + " has already borrowed " + MAX_BORROW_LIMIT + " books (limit reached).");
            return false;
        }

        // ── Validate book ─────────────────────────────────────────────────
        Book book = bookCatalog.get(isbn);
        if (book == null) {
            System.out.println("  [ERROR] Book ISBN " + isbn + " not found.");
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("  [UNAVAILABLE] \"" + book.getTitle() + "\" is already borrowed.");
            return false;
        }

        // ── Update in-memory state ────────────────────────────────────────
        book.setAvailable(false);
        member.addBorrowedBook(book);

        // ── Task 5: persist to database ───────────────────────────────────
        try {
            borrowBookDB(memberId, isbn);
        } catch (SQLException e) {
            // Roll back in-memory change if DB write fails
            book.setAvailable(true);
            member.removeBorrowedBook(book);
            System.out.println("  [DB ERROR] Could not record borrow: " + e.getMessage());
            return false;
        }

        System.out.println("  [SUCCESS] \"" + book.getTitle()
                + "\" borrowed by " + member.getName()
                + " (thread: " + Thread.currentThread().getName() + ")");
        return true;
    }

    //Task 5: Return a book
    public synchronized boolean returnBook(int memberId, String isbn) {

        Member member = memberRegistry.get(memberId);
        if (member == null) {
            System.out.println("  [ERROR] Member ID " + memberId + " not found.");
            return false;
        }

        Book book = bookCatalog.get(isbn);
        if (book == null) {
            System.out.println("  [ERROR] Book ISBN " + isbn + " not found.");
            return false;
        }

        // Confirm the member actually borrowed this book
        boolean memberHasBook = member.getBorrowedBooks().stream()
                .anyMatch(b -> b.getIsbn().equals(isbn));
        if (!memberHasBook) {
            System.out.println("  [ERROR] " + member.getName()
                    + " did not borrow \"" + book.getTitle() + "\".");
            return false;
        }

        // Update in-memory state
        book.setAvailable(true);
        member.removeBorrowedBook(book);

        // Task 5: persist return date to database
        try {
            returnBookDB(memberId, isbn);
        } catch (SQLException e) {
            // Roll back in-memory change
            book.setAvailable(false);
            member.addBorrowedBook(book);
            System.out.println("  [DB ERROR] Could not record return: " + e.getMessage());
            return false;
        }

        System.out.println("  [RETURNED] \"" + book.getTitle()
                + "\" returned by " + member.getName());
        return true;
    }

    // ── Task 5: JDBC — private persistence methods ────────────────────────────

    /**
     * Inserts a borrowing_records row and marks the book unavailable in the database.
     * Uses try-with-resources so the connection is always closed (Task 5).
     */
    private void borrowBookDB(int memberId, String isbn) throws SQLException {

        // language=SQL
        final String UPDATE_BOOK =
                "UPDATE books SET available = FALSE WHERE isbn = ?";

        // language=SQL
        final String INSERT_RECORD =
                "INSERT INTO borrowing_records (member_id, isbn, borrow_date) "
                        + "VALUES (?, ?, ?)";

        // try-with-resources ensures Connection is closed even on exception
        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);   // treat both statements as one transaction

            try (PreparedStatement psBook = conn.prepareStatement(UPDATE_BOOK)) {
                psBook.setString(1, isbn);
                psBook.executeUpdate();
            }

            try (PreparedStatement psRecord = conn.prepareStatement(INSERT_RECORD)) {
                psRecord.setInt(1, memberId);
                psRecord.setString(2, isbn);
                psRecord.setDate(3, Date.valueOf(LocalDate.now()));
                psRecord.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Sets the return_date on the open borrowing record and
     * marks the book available again in the database
     * Uses try-with-resources so the connection is always closed (Task 5).
     */
    private void returnBookDB(int memberId, String isbn) throws SQLException {

        // language=SQL
        final String UPDATE_RECORD =
                "UPDATE borrowing_records "
                        + "SET return_date = ? "
                        + "WHERE member_id = ? AND isbn = ? AND return_date IS NULL";

        // language=SQL
        final String UPDATE_BOOK =
                "UPDATE books SET available = TRUE WHERE isbn = ?";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement psRecord = conn.prepareStatement(UPDATE_RECORD)) {
                psRecord.setDate(1, Date.valueOf(LocalDate.now()));
                psRecord.setInt(2, memberId);
                psRecord.setString(3, isbn);
                psRecord.executeUpdate();
            }

            try (PreparedStatement psBook = conn.prepareStatement(UPDATE_BOOK)) {
                psBook.setString(1, isbn);
                psBook.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            throw e;
        }
    }

    // Convenience: load catalog from DB

    /**
     * Reads all books from the database and loads them into the in-memory catalog.
     * Call this once at startup so the catalog mirrors what is in MySQL.
     */
    public void loadBooksFromDB() throws SQLException {
        final String SQL = "SELECT isbn, title, author, publication_year, available FROM books";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            bookCatalog.clear();
            while (rs.next()) {
                Book book = new Book(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("publication_year")
                );
                book.setAvailable(rs.getBoolean("available"));
                bookCatalog.put(book.getIsbn(), book);
            }
            System.out.println("  [DB] Loaded " + bookCatalog.size() + " book(s) from database.");
        }
    }

    /**
     * Reads all members from the database and loads them into the in-memory registry.
     */
    public void loadMembersFromDB() throws SQLException {
        final String SQL = "SELECT member_id, name FROM members";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            memberRegistry.clear();
            while (rs.next()) {
                Member member = new Member(
                        rs.getInt("member_id"),
                        rs.getString("name")
                );
                memberRegistry.put(member.getMemberId(), member);
            }
            System.out.println("  [DB] Loaded " + memberRegistry.size() + " member(s) from database.");
        }
    }
}
