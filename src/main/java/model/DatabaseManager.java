package model;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DatabaseManager implements MissionRepository, BriefRepository, FacilityRepository, AgentRepository, AuditLogRepository {

    private final String dbUrl;
    private Connection sharedConnection;

    public DatabaseManager() {
        this.dbUrl = "jdbc:sqlite:missions.db";
    }

    public DatabaseManager(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
    }

    public Connection getConnection() throws SQLException {
        if (sharedConnection != null && !sharedConnection.isClosed()) {
            return sharedConnection;
        }
        sharedConnection = DriverManager.getConnection(dbUrl);
        return sharedConnection;
    }

    public void initializeDatabase() throws SQLException, IOException {
        String createFacilityTableSQL = """
            CREATE TABLE IF NOT EXISTS facilities(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                abbreviation TEXT NOT NULL
            );
        """;
        String createAgentTableSQL = """
            CREATE TABLE IF NOT EXISTS agents(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                date_of_birth TEXT,
                date_of_death TEXT,
                notes TEXT,
                facility_id INTEGER NOT NULL,
                FOREIGN KEY (facility_id) REFERENCES facilities(id)
                );
            """;
        String createMissionTableSQL = """
        CREATE TABLE IF NOT EXISTS missions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            date TEXT,
            description TEXT,
            facility_id INTEGER NOT NULL,
            FOREIGN KEY (facility_id) REFERENCES facilities(id)
        );
        """;
        String createBriefTableSQL = """
        CREATE TABLE IF NOT EXISTS briefs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            date TEXT,
            text TEXT NOT NULL,
            mission_id INTEGER NOT NULL,
            FOREIGN KEY (mission_id) REFERENCES missions(id)
        );
        """;
        //add a conjunction table of mission and agent
        String createMissionAgentsSQL = """
                CREATE TABLE IF NOT EXISTS mission_agents (
                mission_id INTEGER NOT NULL,
                agent_id INTEGER NOT NULL,
                PRIMARY KEY (mission_id, agent_id),
                FOREIGN KEY (mission_id) REFERENCES missions(id),
                FOREIGN KEY (agent_id) REFERENCES agents(id)
                );
                """;
        String createAuditLogTableSQL = """
            CREATE TABLE IF NOT EXISTS audit_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_type TEXT NOT NULL,
                content_type TEXT NOT NULL,
                content_id INTEGER NOT NULL,
                username TEXT NOT NULL,
                timestamp TEXT NOT NULL
            );
        """;
        //create the tables in proper order (start with facility)
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createFacilityTableSQL);
            stmt.execute(createAgentTableSQL);
            stmt.execute(createMissionTableSQL);
            stmt.execute(createBriefTableSQL);
            stmt.execute(createMissionAgentsSQL);
            stmt.execute(createAuditLogTableSQL);
        }
        if(getFacilityCount() == 0){
            importFacilitiesFromTsv("data/facilities.tsv");
        }
        if(getAgentCount() == 0){
            importAgentsFromTSV("data/agents.tsv");
        }
        if (getMissionCount() == 0) {
            importMissionsFromTsv("data/mission_briefs.tsv");
        }
    }

    //Mission Methods
    public void importMissionsFromTsv(String filePath) throws IOException, SQLException {
        Scanner scanner = new Scanner(Paths.get(filePath));

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\t", -1);

            if (parts.length != 3) {
                continue;
            }

            String title = parts[0].trim();
            String date = parts[1].trim();
            String briefText = parts[2].trim();

            int facilityId = 1;

            int missionId = addMissionAndReturnId(title, date, "Imported mission.", facilityId);

            addBrief(title + " Brief", date, briefText, missionId);
        }

        scanner.close();
    }

    public int addMissionAndReturnId(String title, String date, String description, int facilityId) throws SQLException {
        String insertSQL = """
            INSERT INTO missions(title, date, description, facility_id)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, date);
            pstmt.setString(3, description);
            pstmt.setInt(4, facilityId);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Could not create mission.");
    }

    @Override
    public void addMission(String title, String date, String description, int facilityId) throws SQLException {
        String insertSQL = """
            INSERT INTO missions(title, date, description, facility_id)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, title);
            pstmt.setString(2, date);
            pstmt.setString(3, description);
            pstmt.setInt(4, facilityId);
            pstmt.executeUpdate();
        }
    }

    public int getMissionCount() throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM missions";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }

    public void clearMissions() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM mission_agents");
            stmt.executeUpdate("DELETE FROM briefs");
            stmt.executeUpdate("DELETE FROM missions");

            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='mission_agents'");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='briefs'");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='missions'");
        }
    }

    @Override
    public List<Mission> getAllMissions() throws SQLException {
        List<Mission> missions = new ArrayList<>();

        String sql = """
            SELECT id, title, date, description, facility_id
            FROM missions
            ORDER BY id
            """;

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                missions.add(new Mission(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("date"),
                        rs.getString("description"),
                        rs.getInt("facility_id")
                ));
            }
        }

        return missions;
    }

    @Override
    public Mission getMissionById(int id) throws SQLException {
        String sql = """
            SELECT id, title, date, description, facility_id
            FROM missions
            WHERE id = ?
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Mission(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("date"),
                            rs.getString("description"),
                            rs.getInt("facility_id")
                    );
                }
            }
        }

        return null;
    }

    @Override
    public List<Mission> getMissionsByFacility(int facilityId) throws SQLException {
        List<Mission> missions = new ArrayList<>();

        String sql = """
            SELECT id, title, date, description, facility_id
            FROM missions
            WHERE facility_id = ?
            ORDER BY id
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, facilityId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    missions.add(new Mission(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("date"),
                            rs.getString("description"),
                            rs.getInt("facility_id")
                    ));
                }
            }
        }

        return missions;
    }

    @Override
    public void updateMission(int id, String title, String date, String description, int facilityId) throws SQLException {
        String updateSQL = """
            UPDATE missions
            SET title = ?, date = ?, description = ?, facility_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(updateSQL)) {
            pstmt.setString(1, title);
            pstmt.setString(2, date);
            pstmt.setString(3, description);
            pstmt.setInt(4, facilityId);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Agent> getAgentsByMission(int missionId)throws SQLException {
        List<Agent>agents = new ArrayList<>();
        String sql = """
                SELECT a.id, a.name, a.date_of_birth, a.date_of_death, a.notes, a.facility_id
                FROM agents a JOIN mission_agents ma ON a.id=ma.agent_id WHERE ma.mission_id = ? ORDER BY a.id
                """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, missionId);
            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    agents.add(new Agent(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("date_of_birth"),
                            rs.getString("date_of_death"),
                            rs.getString("notes"),
                            rs.getInt("facility_id")
                    ));
                }
            }
        }
        return agents;
    }
    @Override
    public void addAgentToMission(int missionId, int agentId) throws SQLException {
        String insertSQL = "INSERT INTO mission_agents(mission_id, agent_id) VALUES (?, ?)";
        try(PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setInt(1, missionId);
            pstmt.setInt(2, agentId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void removeAgentFromMission(int missionId, int agentId) throws SQLException {
        String deleteSQL = "DELETE FROM mission_agents WHERE mission_id = ? AND agent_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(deleteSQL)) {
            pstmt.setInt(1, missionId);
            pstmt.setInt(2, agentId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteMission(int id) throws SQLException {
        String deleteLinksSQL = "DELETE FROM mission_agents WHERE mission_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(deleteLinksSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }

        String deleteBriefsSQL = "DELETE FROM briefs WHERE mission_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(deleteBriefsSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }

        String deleteMissionSQL = "DELETE FROM missions WHERE id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(deleteMissionSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

//Facility Methods
    public void importFacilitiesFromTsv(String filePath) throws IOException, SQLException {
        Scanner scanner = new Scanner(Paths.get(filePath));
        if(scanner.hasNextLine()){
            scanner.nextLine();
        }
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if (line.isBlank())continue;
            String[] parts = line.split("\t", -1);
            if(parts.length != 2) continue;
            String name = parts[0].trim();
            String abbreviation = parts[1].trim();
            addFacility(name, abbreviation);
        }
        scanner.close();
    }
    @Override
    public void addFacility(String name, String abbreviation) throws SQLException {
        String insertSQL= " INSERT INTO facilities (name, abbreviation) VALUES (?, ?)";
        try(PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, abbreviation);
            pstmt.executeUpdate();
        }
    }
    public int getFacilityCount() throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM facilities";
        try(Statement stmt = getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(countSQL);
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }

    public void clearFacilities() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM facilities");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='facilities'");
        }
    }
    @Override
    public List<Facility> getAllFacilities() throws SQLException {
        List<Facility> facilities = new ArrayList<>();
        String sql = "SELECT id, name, abbreviation FROM facilities ORDER BY id";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                facilities.add(new Facility(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("abbreviation")
                ));
            }
        }
        return facilities;
    }
    @Override
    public Facility getFacilityById(int id) throws SQLException {
        String sql = "SELECT id, name, abbreviation FROM facilities WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Facility(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("abbreviation")
                    );
                }
            }
        }
        return null;
    }
    @Override
    public void deleteFacility(int id) throws SQLException{
        //if agents still tied to facility cannot delete
        String agentCheckSQL = "SELECT COUNT (*) FROM agents WHERE facility_id=?";
        try(PreparedStatement pstmt = getConnection().prepareStatement(agentCheckSQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next() && rs.getInt(1)>0){
                    System.out.println("Cannot delete facility because agents are still assigned to it.");
                    return;
                }
            }
        }
        //if missions are assigned to facility cannot delete either
        String missionCheckSQL = "SELECT COUNT (*) FROM missions WHERE facility_id=?";
        try(PreparedStatement pstmt = getConnection().prepareStatement(missionCheckSQL)) {
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next() && rs.getInt(1) >0){
                    System.out.println("Cannot delete facility because missions are still assigned to it.");
                    return;
                }
            }
        }
        //safe to delete otherwise
        String deleteSQL = "DELETE FROM facilities WHERE id = ?";
        try(PreparedStatement pstmt = getConnection().prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    //Agent Methods
    public void importAgentsFromTSV(String filepath) throws SQLException, IOException {
        Scanner scanner = new Scanner(Paths.get(filepath));
        if(scanner.hasNextLine()){
            scanner.nextLine();
        }
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.isBlank()) continue;
            String [] parts = line.split("\t", -1);
            if(parts.length != 2) continue;
            String name = parts[0].trim();
            String dateOfBirth = parts[1].trim();
            //date_of_death and notes get updated later
            addAgent(name, dateOfBirth, "", "", 1);
        }
        scanner.close();
    }
    @Override
    public void addAgent(String name, String dateOfBirth, String dateOfDeath, String notes, int facilityId) throws SQLException {
        String insertSQL = """
                INSERT INTO agents(name, date_of_birth, date_of_death, notes, facility_id) VALUES (?,?,?,?,?)
                """;
        try(PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dateOfBirth);
            pstmt.setString(3, dateOfDeath);
            pstmt.setString(4, notes);
            pstmt.setInt(5, facilityId);
            pstmt.executeUpdate();
        }
    }
    public int getAgentCount() throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM agents";
        try(Statement stmt = getConnection().createStatement()){
            ResultSet rs = stmt.executeQuery(countSQL);
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }
    public void clearAgents() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM mission_agents");
            stmt.executeUpdate("DELETE FROM agents");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='mission_agents'");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='agents'");
        }
    }
    @Override
    public List<Agent> getAllAgents() throws SQLException {
        List<Agent> agents = new ArrayList<>();
        String sql = """
                SELECT id, name, date_of_birth, date_of_death, notes, facility_id FROM agents ORDER BY id
                """;
        try(Statement stmt = getConnection().createStatement()){
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                agents.add(new Agent(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("date_of_birth"),
                        rs.getString("date_of_death"),
                        rs.getString("notes"),
                        rs.getInt("facility_id")
                ));
            }
        }
        return agents;
    }
    @Override
    public Agent getAgentById(int id) throws SQLException {
        String sql= """
                SELECT id, name, date_of_birth, date_of_death, notes, facility_id FROM agents WHERE id=?
                """;
        try(PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Agent(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("date_of_birth"),
                            rs.getString("date_of_death"),
                            rs.getString("notes"),
                            rs.getInt("facility_id")
                    );
                }
            }
        }
        return null;
    }
    @Override
    public List<Agent> getAgentsByFacility(int facilityId) throws SQLException{
        List<Agent> agents = new ArrayList<>();
        String sql = """
                SELECT id, name, date_of_birth, date_of_death, notes, facility_id FROM agents WHERE facility_id=? ORDER BY id
                """;
        try(PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, facilityId);
            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    agents.add(new Agent(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("date_of_birth"),
                            rs.getString("date_of_death"),
                            rs.getString("notes"),
                            rs.getInt("facility_id")
                    ));
                }
            }
        }
        return agents;
    }
    @Override
    public void deleteAgent(int id) throws SQLException{
        //cannot delete if agent staffed on mission
        String missionCheckSQL = "SELECT COUNT(*) FROM mission_agents WHERE agent_id=?";
        try(PreparedStatement pstmt = getConnection().prepareStatement(missionCheckSQL)) {
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1)>0){
                    System.out.println("Cannot delete agent because they are actively staffed on a mission.");
                    return;
                }
            }
        }
        //safe to delete
        String deleteSQL = "DELETE FROM agents WHERE id=?";
        try(PreparedStatement pstmt = getConnection().prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void logEvent(String eventType, String contentType, int contentId, String username) throws SQLException {
        String insertSQL = """
            INSERT INTO audit_log(event_type, content_type, content_id, username, timestamp)
            VALUES (?, ?, ?, ?, datetime('now'))
        """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, eventType);
            pstmt.setString(2, contentType);
            pstmt.setInt(3, contentId);
            pstmt.setString(4, username);
            pstmt.executeUpdate();
        }
    }//end of logEvent

    public List<String> getAuditLog() throws SQLException {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT event_type, content_type, content_id, username, timestamp FROM audit_log ORDER BY id";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(String.format("[%s] %s %s (id:%d) by %s",
                        rs.getString("timestamp"),
                        rs.getString("event_type"),
                        rs.getString("content_type"),
                        rs.getInt("content_id"),
                        rs.getString("username")
                ));
            }
        }
        return logs;
    }//end of getAuditLog

    // Brief Methods

    @Override
    public void addBrief(String title, String date, String text, int missionId) throws SQLException {
        String insertSQL = """
            INSERT INTO briefs(title, date, text, mission_id)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, title);
            pstmt.setString(2, date);
            pstmt.setString(3, text);
            pstmt.setInt(4, missionId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public Brief getBriefById(int id) throws SQLException {
        String sql = """
            SELECT id, title, date, text, mission_id
            FROM briefs
            WHERE id = ?
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Brief(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("date"),
                            rs.getString("text"),
                            rs.getInt("mission_id")
                    );
                }
            }
        }

        return null;
    }

    @Override
    public List<Brief> getAllBriefs() throws SQLException {
        List<Brief> briefs = new ArrayList<>();

        String sql = """
            SELECT id, title, date, text, mission_id
            FROM briefs
            ORDER BY id
            """;

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                briefs.add(new Brief(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("date"),
                        rs.getString("text"),
                        rs.getInt("mission_id")
                ));
            }
        }

        return briefs;
    }

    @Override
    public List<Brief> getBriefsByMission(int missionId) throws SQLException {
        List<Brief> briefs = new ArrayList<>();

        String sql = """
            SELECT id, title, date, text, mission_id
            FROM briefs
            WHERE mission_id = ?
            ORDER BY id
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, missionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    briefs.add(new Brief(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("date"),
                            rs.getString("text"),
                            rs.getInt("mission_id")
                    ));
                }
            }
        }

        return briefs;
    }

    @Override
    public void updateBrief(int id, String title, String date, String text, int missionId) throws SQLException {
        String updateSQL = """
            UPDATE briefs
            SET title = ?, date = ?, text = ?, mission_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(updateSQL)) {
            pstmt.setString(1, title);
            pstmt.setString(2, date);
            pstmt.setString(3, text);
            pstmt.setInt(4, missionId);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteBrief(int id) throws SQLException {
        String deleteSQL = "DELETE FROM briefs WHERE id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int getBriefCount() throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM briefs";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    public void clearBriefs() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM briefs");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='briefs'");
        }
    }

}//end of DatabaseManager

