package com.example.repository;

import java.sql.Date;

/**
 * Represents a single Moon Mission entity, containing key historical and technical details
 * retrieved from the persistence layer.
 * <p>
 * This record serves as the data transfer object (DTO) used by the
 * {@code MoonMissionRepository} to carry data from the database back to the application logic.
 * </p>
 * * @param missionId The unique primary key identifier for the mission.
 * @param spacecraft The name of the spacecraft launched during the mission.
 * @param launchDate The date the mission was launched (stored as a String or a date format).
 * @param carrierRocket The name of the rocket used to carry the spacecraft.
 * @param operator The organization or country responsible for the mission (e.g., NASA, USSR).
 * @param missionType The categorization of the mission (e.g., Orbiter, Lander, Flyby).
 * @param outcome The result of the mission (e.g., Success, Failure, Partial Failure).
 */
public record MoonMission(int missionId, String spacecraft, String launchDate,
                          String carrierRocket, String operator, String missionType, String outcome){}
