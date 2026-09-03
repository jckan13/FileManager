package model;

import java.util.List;
import java.sql.SQLException;

public interface MissionRepository {
    //reads
    List<Mission> getAllMissions() throws SQLException;
    Mission getMissionById(int id) throws SQLException;
    List<Mission> getMissionsByFacility(int facilityId) throws SQLException;
    List<Agent>getAgentsByMission(int missionId) throws SQLException;

    //writes
    void addMission(String title, String date, String description, int facilityId) throws SQLException;
    void addAgentToMission(int missionId, int agentId) throws SQLException;
    void removeAgentFromMission(int missionId, int agentId) throws SQLException;
    void deleteMission(int id) throws SQLException;
    void updateMission(int id, String title, String date, String description, int facilityId) throws SQLException;
}