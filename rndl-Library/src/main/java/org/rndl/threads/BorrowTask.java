package org.rndl.threads;

import org.rndl.service.LibraryService;

/**
 * Task 4 – Multithreading
 *
 * BorrowTask implements Runnable so it can be submitted to an ExecutorService.
 * Each instance represents one librarian processing a single borrow request.
 *
 * Thread-safety is guaranteed by the {@code synchronized} keyword on
 * {@link LibraryService#borrowBook}, which prevents two librarians from
 * borrowing the same book simultaneously.
 */
public class BorrowTask implements Runnable {

    // Private fields (encapsulation)
    private final LibraryService service;
    private final int            memberId;
    private final String         isbn;

    // Constructor

    public BorrowTask(LibraryService service, int memberId, String isbn) {
        this.service  = service;
        this.memberId = memberId;
        this.isbn     = isbn;
    }

    // Runnable implementation
    /**
     * Called by the thread pool when a librarian thread is ready.
     * LibraryService.borrowBook() is synchronized, so only one thread
     * enters the critical section at a time — preventing the same book
     * from being issued to two members concurrently.
     */
    @Override
    public void run() {
        System.out.println("[THREAD] " + Thread.currentThread().getName()
                + " → processing borrow request for ISBN: " + isbn
                + " | member ID: " + memberId);

        service.borrowBook(memberId, isbn);
    }
}
