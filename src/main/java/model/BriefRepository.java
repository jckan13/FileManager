package model;

import java.sql.SQLException;
import java.util.List;

public interface BriefRepository {
    void addBrief(String title, String date, String text, int missionId) throws SQLException;

    Brief getBriefById(int id) throws SQLException;

    List<Brief> getAllBriefs() throws SQLException;

    List<Brief> getBriefsByMission(int missionId) throws SQLException;

    void updateBrief(int id, String title, String date, String text, int missionId) throws SQLException;

    void deleteBrief(int id) throws SQLException;
}