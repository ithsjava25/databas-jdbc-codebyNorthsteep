package com.example.repository;

import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for data access operations related to Moon Mission entities.
 * This interface implements the Repository Pattern, abstracting away the underlying
 * data storage details (e.g., JDBC implementation).
 * All methods required for retrieving and aggregating moon mission data should be defined here.
 */
public interface MoonMissionRepository {

    /**
     * Retrieves a list of all spacecraft names for every recorded moon mission.
     * * @return A {@code List} of {@code String} containing the names of the spacecrafts.
     * Returns an empty list if no missions are found.
     * @throws RuntimeException If a database access error occurs during retrieval.
     */
    List<String> listMoonMissions();

    /**
     * Retrieves a specific moon mission based on its unique mission ID.
     * * @param id The unique identifier (mission_id) of the moon mission.
     * @return An {@code Optional} containing the {@code MoonMission} object if a match is found,
     * or {@code Optional.empty()} if no mission exists with the given ID.
     * @throws RuntimeException If a database access error occurs during retrieval.
     */
    Optional<MoonMission> getMoonMissionById(int id);

    /**
     * Counts the total number of moon missions launched in a specific year.
     * * @param year The four-digit year (e.g., 1969) to count missions for.
     * @return The total count of missions launched in that year. Returns 0 if no missions were found.
     * @throws RuntimeException If a database access error occurs during the count operation.
     */
    int countMissionsPerYear(int year);

}

