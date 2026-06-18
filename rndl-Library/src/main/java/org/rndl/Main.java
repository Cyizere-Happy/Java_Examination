package org.rndl;


import org.rndl.models.Book;
import org.rndl.models.Member;
import org.rndl.service.LibraryService;
import org.rndl.threads.BorrowTask;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * RNDL Library Management System — Entry Point
 *
 * Demonstrates all five tasks:
 *   Task 1 – Schema already applied to the PostgreSQL rndl_library database
 *   Task 2 – Encapsulated Book, Member, LibraryService (Singleton) classes
 *   Task 3 – Collections + borrow-limit enforcement
 *   Task 4 – Concurrent BorrowTask threads via ExecutorService
 *   Task 5 – JDBC persistence (borrow + return)
 */
public class Main {

    public static void main(String[] args) {

        // Task 2: Singleton — only one LibraryService instance ever exists
        LibraryService service = LibraryService.getInstance();

        // Task 5: Load books and members from PostgreSQL into memory
        System.out.println("\n Loading data from database: ");
        try {
            service.loadBooksFromDB();
            service.loadMembersFromDB();
        } catch (SQLException e) {
            System.out.println("[FATAL] Cannot connect to database: " + e.getMessage());
            System.out.println("  → Falling back to manual seed data for demonstration.\n");
            seedManualData(service);
        }

        // Task 3: List all available books (uses ArrayList + HashMap)
        System.out.println("\n Task 3: Available books: ");
        List<Book> availableBooks = service.listAvailableBooks();
        if (availableBooks.isEmpty()) {
            System.out.println("  No books currently available.");
        } else {
            availableBooks.forEach(b -> System.out.println("  " + b));
        }

        // Task 4: Concurrent borrowing with ExecutorService
        // Simulates 3 librarians processing requests at the same time.
        // Librarians 1 and 2 both request "Animal Farm" — only ONE should
        // succeed thanks to the synchronized keyword in LibraryService.
        System.out.println("\n Task 4: Concurrent borrow requests: ");

        ExecutorService pool = Executors.newFixedThreadPool(3);

        // Librarian 1: member 1 borrows "Animal Farm"
        pool.submit(new BorrowTask(service, 1, "978-0-14-303943-3"));

        // Librarian 2: member 2 tries to borrow the SAME book → should be DENIED
        pool.submit(new BorrowTask(service, 2, "978-0-14-303943-3"));

        // Librarian 3: member 2 borrows "The Great Gatsby"
        pool.submit(new BorrowTask(service, 2, "978-0-7432-7356-5"));

        // Shut down pool  no new tasks accepted; wait for running ones to finish
        pool.shutdown();
        try {
            pool.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("[WARN] Thread pool interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        // Task 3: Borrow-limit test — try to push member 1 past 5 books
        // Member 1 already holds "Animal Farm" (1 book).
        // We try 4 more + 1 extra to trigger the [DENIED] limit message.
        System.out.println("\n Task 3: Borrow limit test (member 1): ");
        String[] extraIsbns = {
                "978-0-14-028329-7",   // Of Mice and Men        → borrow #2
                "978-0-14-243723-0",   // The Alchemist          → borrow #3
                "978-0-06-112008-4",   // To Kill a Mockingbird  → borrow #4
                "978-0-14-303943-3",   // Animal Farm — already borrowed → DENIED (unavailable)
                "978-0-7432-7356-5"    // The Great Gatsby — borrowed by member 2 → DENIED
        };
        for (String isbn : extraIsbns) {
            service.borrowBook(1, isbn);
        }

        // ── Task 5: Return a book — confirm DB updated + book available again ──
        // Member 1 always borrows "Of Mice and Men" in the sequential section above,
        // so this return is guaranteed to succeed regardless of thread ordering.
        System.out.println("\n Task 5: Return a book ");
        service.returnBook(1, "978-0-14-028329-7");   // member 1 returns "Of Mice and Men"

        // Final state
        System.out.println("\n Final available books: ");
        service.listAvailableBooks()
                .forEach(b -> System.out.println("  " + b));

        System.out.println("\n══════ System demo complete ══════\n");
    }

    // Fallback seed — used when the database is unreachable
    private static void seedManualData(LibraryService service) {
        service.addBook(new Book("978-0-14-303943-3", "Animal Farm",           "George Orwell",        1945));
        service.addBook(new Book("978-0-7432-7356-5", "The Great Gatsby",      "F. Scott Fitzgerald",  1925));
        service.addBook(new Book("978-0-14-028329-7", "Of Mice and Men",       "John Steinbeck",       1937));
        service.addBook(new Book("978-0-14-243723-0", "The Alchemist",         "Paulo Coelho",         1988));
        service.addBook(new Book("978-0-06-112008-4", "To Kill a Mockingbird", "Harper Lee",           1960));

        service.addMember(new Member(1, "Alice Uwimana"));
        service.addMember(new Member(2, "Bob Nkurunziza"));
        service.addMember(new Member(3, "Claire Mukamana"));

        System.out.println("  [SEED] 5 books and 3 members loaded (manual fallback).");
    }
}