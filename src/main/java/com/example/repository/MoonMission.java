package com.example.repository;

import java.sql.Date;

public record MoonMission(int missionId, String spacecraft, String launchDate,
                          String carrierRocket, String operator, String missionType, String outcome){}
