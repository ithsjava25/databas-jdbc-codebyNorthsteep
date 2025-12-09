package com.example.repository;

import com.example.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoonMissionRepositoryImplJdbc implements  MoonMissionRepository {
    private final DataSource dataSource;

    public MoonMissionRepositoryImplJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<String> listMoonMissions() {
        List<String> spacecrafts = new ArrayList<>();
        String query = "select spacecraft from moon_mission";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                spacecrafts.add(result.getString("spacecraft"));
            }
            return spacecrafts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<MoonMission> getMoonMissionById(int missionId) {

        String query = "select * from moon_mission where mission_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, missionId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    int id = result.getInt("mission_id");
                    String spacecraft = result.getString("spacecraft");
                    Date sqlLaunchDate = result.getDate("launch_date");
                    String launchDate = (sqlLaunchDate == null) ? null : sqlLaunchDate.toString();
                    String carrierRocket = result.getString("carrier_rocket");
                    String operator = result.getString("operator");
                    String missionType = result.getString("mission_type");
                    String outcome = result.getString("outcome");

                    MoonMission moonMission = new MoonMission(id, spacecraft, launchDate, carrierRocket, operator, missionType, outcome);
                    return Optional.of(moonMission);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving mission: " + e);
        }
        return Optional.empty();
    }

    @Override
    public int countMissionsPerYear(int year) {

        String query = "select count(*) as numberOfMissions from moon_mission where launch_date like ?";


        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, year + "%");
            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("numberOfMissions");

                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count missions for year " + year + ":" + e);
        }

    }
}
