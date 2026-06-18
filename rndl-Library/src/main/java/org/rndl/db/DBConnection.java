package org.rndl.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Task 5 – JDBC
 * a single static factory method for obtaining a database connection.
 */
public class DBConnection {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/rndl_library";
    private static final String DB_USER     = "postgres";
    private static final String DB_PASSWORD = "5Tr@wberry";

    // Private constructor – utility class, not meant to be instantiated
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
