package com.example;

import com.example.repository.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        AccountRepository accountRepository = new AccountRepositoryImplJdbc(dataSource);
        MoonMissionRepository moonMissionRepository = new MoonMissionRepositoryImplJdbc(dataSource);


        try (Scanner scanner = new Scanner(System.in)) {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("Connected to the database");
            }

            if (!authenticationForUser(accountRepository, scanner)) {
                System.out.println("Username or password is incorrect. Exiting application...");
                return;
            }
            runOptionMenu(accountRepository,moonMissionRepository, scanner);

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed." + e);
        }

        //TODO: Lyfta ut all kod som pratar med databasen från main
        // och placera dem i specialiserade klasser som implementerar definierade interfaces.



    }

    private boolean authenticationForUser(AccountRepository accountRepository, Scanner scanner) {
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
            if(accountRepository.authenticationForUser(username, password)) {
                System.out.println("Login successful.");
                return true;
            } else {
                System.out.println("Invalid username or password. Please try again or enter '0' to exit.");
            }
        }

    }

    private void runOptionMenu(AccountRepository accountRepository,MoonMissionRepository moonMissionRepository, Scanner scanner) {

        System.out.println("Welcome to the CLI - Database");
        while (true) {

            optionMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listMoonMissions(moonMissionRepository);
                case "2" -> getMoonMissionById(moonMissionRepository, scanner);
                case "3" -> countMissionsPerYear(moonMissionRepository, scanner);
                case "4" -> createAccount(accountRepository, scanner);
                case "5" -> updateAccount(accountRepository, scanner);
                case "6" -> deleteAccount(accountRepository, scanner);
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

    private void listMoonMissions(MoonMissionRepository moonMissionRepository) {
        List<String> moonList = moonMissionRepository.listMoonMissions();
        try {
            if (moonList.isEmpty()) {
                System.out.println("No moon missions found.");
            } else {
                System.out.println("Moon missions:");
                moonList.forEach(System.out::println);
            }
        }catch (Exception e) {
            throw new RuntimeException("Failed to list moon missions.", e);
        }
    }

    private void getMoonMissionById(MoonMissionRepository moonMissionRepository, Scanner scanner) {
        System.out.print("Please enter the moon mission id: ");

        int missionId;
        try {
            missionId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid mission ID. Please enter a number.");
            return;
        }

        try {
            Optional<MoonMission> missionById = moonMissionRepository.getMoonMissionById(missionId);
            if(missionById.isPresent()) {
                MoonMission mission = missionById.get();
                System.out.println("Moon mission details:");
                System.out.printf("Mission ID: %d\n", mission.missionId());
                System.out.printf("Spacecraft: %s\n", mission.spacecraft());
                System.out.printf("Launch date: %s\n", mission.launchDate());
                System.out.printf("Carrier rocket: %s\n", mission.carrierRocket());
                System.out.printf("Operator: %s\n", mission.operator());
                System.out.printf("Mission type: %s\n", mission.missionType());
                System.out.printf("Outcome: %s\n", mission.outcome());

                } else {
                    System.out.println("No mission found with ID: " + missionId);
                }

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving mission" + e);
        }
    }

    private void countMissionsPerYear(MoonMissionRepository moonMissionRepository, Scanner scanner) {
        System.out.println("Please select year (e.g 1958): ");

        int year;
        try {
            year = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid year. Please enter a number.");
            return;
        }

        try  {
            int count = moonMissionRepository.countMissionsPerYear(year);
            System.out.printf("Number of moon missions for %d: %d\n", year, count);

        } catch (Exception e) {
            throw new RuntimeException("Failed to count missions for year " + year + e);
        }
    }

    private void createAccount(AccountRepository accountRepository, Scanner scanner) {

        System.out.println("Please enter your account information: ");


        try {

            System.out.println("Enter your account first name: ");
            String firstName = scanner.nextLine().trim();

            System.out.println("Enter your account last name: ");
            String lastName = scanner.nextLine().trim();
            System.out.println("Enter your account ssn(10 digits xxxxxx-xxxx): ");
            String ssn = scanner.nextLine().trim();

            System.out.println("Enter your account password: ");
            String password = scanner.nextLine().trim();


            int newId = accountRepository.createAccount(firstName, lastName, ssn, password);
            if (newId > 0) {
                System.out.print("Account created successfully!\n");
            } else {
                System.out.println("Failed to create account.");
            }
        }catch (Exception e) {
            throw new RuntimeException("Failed to create account information.", e);
        }
        }

    private void updateAccount(AccountRepository accountRepository, Scanner scanner) {
        //Update an account password (prompts: user_id, new password; prints confirmation).

        System.out.println("Please enter your user id to change password: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid user id entered. Please enter a numeric id.");
            return;
        }

        System.out.println("Please enter your new account password: ");
        String newPassword = scanner.nextLine();
        if (newPassword == null || newPassword.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        try  {

            boolean rowsIsUpdated = accountRepository.updateAccount(id, newPassword);
            if (rowsIsUpdated) {
                System.out.println("Your account password has been updated");
            } else {
                System.out.printf("No account found with ID: %d", id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error trying to change password. " + e);
        }
    }

    private void deleteAccount(AccountRepository accountRepository, Scanner scanner) {

        System.out.println("Please enter your user id to delete account: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid user id entered. Please enter a numeric id.");
            return;
        }

        try  {

            boolean rowsIsDeleted = accountRepository.deleteAccount(id);
            if (rowsIsDeleted) {
                System.out.println("Your account has been deleted");
            } else {
                System.out.printf("No account found with ID: %d", id);
            }
        } catch (Exception e) {
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
