package com.example.repository;

import com.example.DataSource;

import java.sql.*;

/**
 * Implementation of the AccountRepository interface using JDBC.
 * This class is responsible for all persistence and retrieval operations
 * related to user accounts in the database.
 */
public class AccountRepositoryImplJdbc implements AccountRepository {
    private final DataSource dataSource;

    /**
     * Constructs the repository with a specific DataSource.
     * The DataSource provides connections to the database.
     *  @param dataSource The source for database connections.
     */
    public AccountRepositoryImplJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses a SQL SELECT query via JDBC to validate credentials.
     * </p>
     */
    @Override
    public boolean authenticationForUser(String username, String password) {
        //Note for CodeRabbit: production code should use password hashing in further databases.

        String query = "select name, password from account where name = ? and password = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Authentication failed." + e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses JDBC and {@code Statement.RETURN_GENERATED_KEYS}
     * to retrieve the generated {@code user_id} upon successful insertion.
     * Also adds a suffix to the name if the name already exists.
     * </p>
     */
    @Override
    public int createAccount(String firstname, String lastname, String ssn, String password) {
        //Note for CodeRabbit: production code should use password hashing in further databases.

        String query = "insert into account(name,first_name, last_name, ssn, password) values (?,?,?,?,?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            String firstPart = firstname.length() >= 3 ? firstname.substring(0,3) : firstname;
            String lastPart = lastname.length() >= 3 ? lastname.substring(0,3) : lastname;
            String name = firstPart + lastPart;
            int suffix = 1;

            while (usernameExists(name)) {
                name = name + suffix++;
            }

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, firstname);
            preparedStatement.setString(3, lastname);
            preparedStatement.setString(4, ssn);
            preparedStatement.setString(5, password);


            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted == 1) {

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }

            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Error trying to create account. " + e);

        }
    }

    /**
     * Helper-method for createAccount().
     * Uses COUNT to check the database if the name already exists.
     * @param name The generated name to be checked.
     * @return True if the name already exists, else false.
     */
    public boolean usernameExists(String name) {
        // Använd COUNT för att snabbt se om namnet redan finns
        String query = "select count(name) from account where name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during username uniqueness check: " + e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses a SQL UPDATE statement and checks if exactly one row was updated.
     * </p>
     */
    @Override
    public boolean updateAccount(int userId, String newPassword) {
        String query = "update account set password = ? where user_id = ?";


        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, userId);

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error trying to change password. " + e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses a SQL DELETE statement and checks if exactly one row was deleted.
     * </p>
     */
    @Override
    public boolean deleteAccount(int userId) {
        String query = "delete from account where user_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Error trying to delete account. " + e);
        }
    }
}
