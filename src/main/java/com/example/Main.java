package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }


        try (Scanner scanner = new Scanner(System.in); Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            System.out.println("Connected to the database");

            if (!authenticationForUser(connection, scanner)) {
                System.out.println("Username or password is incorrect. Exiting application...");
                return;
            }
            runOptionMenu(connection, scanner);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private boolean authenticationForUser(Connection connection, Scanner scanner) {
        //Note from CodeRabbit - production code should use password hashing.

        while (true) {
            System.out.print("Please enter the top secret username: ");
            String username = scanner.nextLine().trim();
            if (username.equals("0")) {
                return false;
            }
            System.out.print("Please enter the top secret password: ");
            String password = scanner.nextLine().trim();
            if (password.equals("0")) {
                return false;
            }

            String query = "SELECT name, password FROM account WHERE name = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        System.out.println("Logged in successfully.");
                        return true;
                    } else {
                        System.out.println("Invalid username or password. Try again, or exit with '0'.");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void runOptionMenu(Connection connection, Scanner scanner) {

        System.out.println("Welcome to the CLI - Database");
        while (true) {

            optionMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listMoonMissions(connection);
                case "2" -> getMoonMissionById(connection, scanner);
                case "3" -> countMissionsPerYear(connection, scanner);
                case "4" -> createAccount(connection, scanner);
                case "5" -> updateAccount(connection, scanner);
                case "6" -> deleteAccount(connection, scanner);
                case "0" -> {
                    System.out.println("Exiting application...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");

            }
        }
    }

    private void optionMenu() {

        System.out.println("Please select an option:");
        System.out.println("1) List moon missions (prints spacecraft names from `moon_mission`).");
        System.out.println("2) Get a moon mission by mission_id (prints details for that mission).");
        System.out.println("3) Count missions for a given year (prompts: year; prints the number of missions launched that year).");
        System.out.println("4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).");
        System.out.println("5) Update an account password (prompts: user_id, new password; prints confirmation).");
        System.out.println("6) Delete an account (prompts: user_id; prints confirmation).");
        System.out.println("0) Exit.");

    }

    private void listMoonMissions(Connection connection) {
        String query = "select spacecraft from moon_mission";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    System.out.println(result.getString("spacecraft"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("No data found." + e);
        }
    }

    private void getMoonMissionById(Connection connection, Scanner scanner) {
        System.out.print("Please enter the moon mission id: ");

        int missionId;
        try {
            missionId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid mission ID. Please enter a number.");
            return;
        }

        String query = "select * from moon_mission where mission_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, missionId);
            try (ResultSet result = statement.executeQuery()) {

                ResultSetMetaData metaData = result.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (result.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(metaData.getColumnLabel(i) + "\t");
                    }
                    System.out.println();
                    for (int i = 1; i <= columnCount; i++) {
                        Object columnValue = result.getObject(i);
                        System.out.print(columnValue + "\t");
                    }
                } else {
                    System.out.println("No mission found with ID: " + missionId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving mission" + e);
        }
    }

    private void countMissionsPerYear(Connection connection, Scanner scanner) {
        System.out.println("Please select year (e.g 1958): ");
        String query = "select count(*) as numberOfMissions from moon_mission where launch_date like ?";
        int year;

        try {
            year = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid year. Please enter a number.");
            return;
        }


        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, year + "%");
            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    int count = result.getInt("numberOfMissions");
                    System.out.printf("Number of missions launched in %d: %d%n", year, count);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count missions for year " + year + e);
        }
    }

    private void createAccount(Connection connection, Scanner scanner) {
        String query = "insert into account(password, first_name, last_name, ssn) values (?,?,?,?)";
        System.out.println("Please enter your account information: ");


        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("Enter your account first name: ");
            preparedStatement.setString(2, scanner.nextLine().trim());

            System.out.println("Enter your account last name: ");
            preparedStatement.setString(3, scanner.nextLine().trim());

            System.out.println("Enter your account ssn(10 digits xxxxxx-xxxx): ");
            preparedStatement.setString(4, scanner.nextLine().trim());

            System.out.println("Enter your account password: ");
            preparedStatement.setString(1, scanner.nextLine().trim());


            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted == 1) {

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        long newID = resultSet.getLong(1);
                        System.out.println(("User created with generated ID:" + newID));
                    } else {
                        System.out.println("No key generated");
                    }
                }
            } else {
                System.out.println("Insert failed");
            }


        } catch (SQLException e) {
            throw new RuntimeException("Error trying to create account. " + e);
        }
    }

    private void updateAccount(Connection connection, Scanner scanner) {
        //Update an account password (prompts: user_id, new password; prints confirmation).
        String query = "update account set password = ? where user_id = ?";

        System.out.println("Please enter your user id to change password: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        }catch (NumberFormatException e) {
            System.out.println("Invalid user id entered. Please enter a numeric id.");
            return;
        }

        System.out.println("Please enter your new account password: ");
        String newPassword = scanner.nextLine();
        if (newPassword == null || newPassword.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, id);

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated == 1) {
                System.out.println("Your account password has been updated");
            } else {
                System.out.printf("No account found with ID: %d", id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error trying to change password. " + e);
        }
    }

    private void deleteAccount(Connection connection, Scanner scanner) {
        String query = "delete from account where user_id = ?";
        System.out.println("Please enter your user id to delete account: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        }catch (NumberFormatException e) {
            System.out.println("Invalid user id entered. Please enter a numeric id.");
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted == 1) {
                System.out.println("Your account has been deleted");
            } else  {
                System.out.printf("No account found with ID: %d", id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error trying to delete account. " + e);
        }
    }

    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
