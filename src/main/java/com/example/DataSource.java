package com.example;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Standard interface defining the contract for obtaining a connection to a data source (typically a database).
 * <p>
 * This interface is a crucial part of the Dependency Inversion Principle,
 * abstracting the connection details so that the application's repository code
 * does not depend directly on the specific network connection setup (e.g., DriverManager).
 * </p>
 * When a repository calls {@code getConnection()}, it receives a ready-to-use {@code Connection} instance.
 *
 * @author [Ditt Namn]
 * @version 1.0
 */
public interface DataSource {

    /**
     * Attempts to establish a connection to the data source.
     * <p>
     * Implementations of this method should handle the underlying connection pool or
     * driver management and return an active connection.
     * </p>
     * * @return A new and active {@code Connection} instance.
     * @throws SQLException If a database access error occurs, or if the connection fails
     * to be established.
     */
    Connection getConnection() throws SQLException;
}
