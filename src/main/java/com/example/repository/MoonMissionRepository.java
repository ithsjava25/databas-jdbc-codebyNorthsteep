package com.example.repository;

import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {
    List<String> listMoonMissions();
    Optional<MoonMission> getMoonMissionById(int id);
    int countMissionsPerYear(int year);

}

