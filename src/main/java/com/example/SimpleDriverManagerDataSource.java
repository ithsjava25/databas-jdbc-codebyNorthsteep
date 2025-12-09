package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Concrete implementation of the {@code DataSource} interface that uses the standard
 * {@code DriverManager} to establish new database connections.
 * <p>
 * This class functions as a **factory** for database connections based on the JDBC
 * configuration details (URL, user, password) provided at startup. By encapsulating
 * the {@code DriverManager}, this class ensures that repositories only depend on
 * the {@code DataSource} interface, promoting **decoupling**.
 * </p>
 *
 * @see DataSource
 */
public class SimpleDriverManagerDataSource implements DataSource {
    String url;
    String user;
    String password;

    /**
     * Constructs the data source with the required JDBC connection parameters.
     * These configuration values are stored and used every time a new connection is requested.
     *
     * @param url The JDBC URL for the target database (e.g., "jdbc:mysql://localhost:3306/mydb").
     * @param user The database username.
     * @param password The database password.
     */
    public SimpleDriverManagerDataSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Obtains a new database connection by forwarding the stored URL, user, and password
     * to the underlying {@code DriverManager.getConnection()}.
     * </p>
     *
     * @return A new and active {@code Connection} instance.
     * @throws SQLException If a database access error occurs during the connection attempt.
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);

    }
}
