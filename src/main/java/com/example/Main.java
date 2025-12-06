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

        Scanner scanner = new Scanner(System.in);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            System.out.println("Connected to the database");

            if (!authenticationForUser(connection, scanner)) {
                System.out.println("Username or password is incorrect. Exiting application...");
                return;
            }
            //TODO: Körbar metod för menyval istället för optionMenu, Case? Körbar while(true)
            runOptionMenu(connection, scanner);
            //return?

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //Todo: Starting point for your code
        //TODO: Skapa en autentisering för inloggning i databasen
        //TODO: Skapa metoder för val i menyn
        //TODO: Switch-sats som kopplar till metoder för val

    }

    private boolean authenticationForUser(Connection connection, Scanner scanner) {
        boolean isLoggedIn = false;
        String username ="";
        String password ="";
        while (!isLoggedIn) {
            System.out.println("Please enter the top secret username: ");
            username = scanner.nextLine().trim();
            if (username.equals("0")) {
                return false;
            }
            System.out.println("Please enter the top secret password: ");
            password = scanner.nextLine();
            if (password.equals("0")) {
                return false;
            }

            //TODO: Hitta varför users ej fungerar
            String sql = "SELECT username, password FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password);
                try(ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        System.out.println("Logged in successfully.");
                        isLoggedIn = true;
                    }
                    else {
                        System.out.println("Username or password is incorrect. Exiting application...");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private void runOptionMenu(Connection connection, Scanner scanner) {
        while (true) {
            optionMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> listMoonMissions(connection, scanner);
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
        System.out.println("Welcome to the CLI - Database");
        System.out.println("Please select an option:");
        System.out.println("1) List moon missions (prints spacecraft names from `moon_mission`).");
        System.out.println("2) Get a moon mission by mission_id (prints details for that mission).");
        System.out.println("3) Count missions for a given year (prompts: year; prints the number of missions launched that year).");
        System.out.println("4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).");
        System.out.println("5) Update an account password (prompts: user_id, new password; prints confirmation).");
        System.out.println("6) Delete an account (prompts: user_id; prints confirmation).");
        System.out.println("0) Exit.");

    }

    private void listMoonMissions(Connection connection, Scanner scanner) {

    }
    private void getMoonMissionById(Connection connection, Scanner scanner) {

    }

    private void countMissionsPerYear(Connection connection, Scanner scanner) {

    }

    private void createAccount(Connection connection, Scanner scanner) {

    }

    private void updateAccount(Connection connection, Scanner scanner) {

    }

    private void deleteAccount(Connection connection, Scanner scanner) {

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
