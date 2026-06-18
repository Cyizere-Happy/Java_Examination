# Rwanda National Digital Library (RNDL) - Library Management System

**Course:** Fundamentals of OOP with Java  
**Component:** Practical Assignment Submission  

---

## 🚀 How to Run the System

1. **Database Setup (Task 1):**
   * Open your PostgreSQL terminal or pgAdmin.
   * Run the script located in `database/schema.sql` to create the `rndl_library` database, build tables, and populate sample records.
   
2. **Application Execution:**
   * Open the project folder in your Java IDE (IntelliJ IDEA or Eclipse).
   * Ensure your PostgreSQL JDBC Driver is added to the project classpath.
   * Update the database password in `src/org/rndl/db/DBConnection.java` if necessary.
   * Run `Main.java`.

---

## 🛠️ Verification of Assignment Tasks

### Task 1: Database Schema Design (5 Marks)
* **File:** `database/schema.sql`
* Implements the `books`, `members`, and `borrowing_records` relational tables with strict Primary Keys, Foreign Keys, and Nullability rules.

### Task 2: Encapsulation (10 Marks)
* **Files:** `Book.java`, `Member.java`, `LibraryService.java`
* Implements strict OOP encapsulation. All fields are `private` with public getters/setters. 
* The `Member` class returns an unmodifiable view via `Collections.unmodifiableList(borrowedBooks)` to secure the collection from external tampering.

### Task 3: Core Logic & Collections (10 Marks)
* **File:** `LibraryService.java` (specifically the `borrowBook` method)
* Uses an in-memory `HashMap` catalog to tracking system items.
* Evaluates `member.getBorrowedCount() >= MAX_BORROW_LIMIT` before allowing any transaction to ensure no member can borrow more than 5 books simultaneously.

### Task 4: Multithreading & Concurrency (5 Marks)
* **Files:** `BorrowTask.java`, `Main.java`
* `BorrowTask` implements `Runnable` to represent individual concurrent librarian transactions.
* Uses an `ExecutorService` thread pool in `Main.java` to process requests simultaneously.
* **Thread Safety:** The public operational entry points in `LibraryService.java` are `synchronized` to provide mutual exclusion, ensuring that two librarians cannot assign the same book to different users at the exact same time.

### Task 5: Data Persistence & JDBC (15 Marks)
* **Files:** `LibraryService.java`, `DBConnection.java`
* Uses raw `java.sql` Core JDBC API with **Try-With-Resources** structural blocks to guarantee that all database connectivity channels automatically close even during unexpected disruptions.
* Implements full transaction processing utilizing manually controlled application rollbacks (`conn.rollback()`) to protect relational database state unity if multi-step insertion updates fail midway.