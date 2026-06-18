-- Task 1 – Relational Database Schema Design
DROP DATABASE IF EXISTS rndl_library;
CREATE DATABASE rndl_library;
\c rndl_library

CREATE TABLE books (
    isbn             VARCHAR(20)  PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    publication_year INT          NOT NULL,
    available        BOOLEAN      DEFAULT TRUE
);

CREATE TABLE members (
    member_id  SERIAL       PRIMARY KEY,
    name       VARCHAR(255) NOT NULL
);

CREATE TABLE borrowing_records (
    record_id   SERIAL      PRIMARY KEY,
    member_id   INT         NOT NULL,
    isbn        VARCHAR(20) NOT NULL,
    borrow_date DATE        NOT NULL,
    return_date DATE,
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_book   FOREIGN KEY (isbn)      REFERENCES books(isbn) ON DELETE CASCADE
);

-- Expanded book collection to support testing the 5-book limit rule
INSERT INTO books (isbn, title, author, publication_year) VALUES
    ('978-0-06-112008-4', 'To Kill a Mockingbird', 'Harper Lee',           1960),
    ('978-0-7432-7356-5', 'The Great Gatsby',      'F. Scott Fitzgerald', 1925),
    ('978-0-14-028329-7', 'Of Mice and Men',       'John Steinbeck',      1937),
    ('978-0-14-303943-3', 'Animal Farm',            'George Orwell',       1945),
    ('978-0-14-243723-0', 'The Alchemist',         'Paulo Coelho',        1988),
    ('978-0-14-044913-6', 'The Odyssey',           'Homer',               2003),
    ('978-0-45152493-5', '1984',                  'George Orwell',       1949),
    ('978-0-14-044926-6', 'The Iliad',             'Homer',               2003);

INSERT INTO members (name) VALUES
    ('Jean Paul Habimana'),
    ('Marie Claire Ingabire'),
    ('Patrick Nzeyimana');

-- Schema verification audit log
SELECT 'BOOKS' AS table_name, COUNT(*) AS total FROM books
UNION ALL
SELECT 'MEMBERS',            COUNT(*) FROM members
UNION ALL
SELECT 'BORROW RECORDS',     COUNT(*) FROM borrowing_records;