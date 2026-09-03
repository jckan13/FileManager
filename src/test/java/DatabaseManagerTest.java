import model.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private DatabaseManager dbManager;


    @BeforeEach
    void setUp() throws SQLException, IOException {
        dbManager = new DatabaseManager(":memory:");
        dbManager.initializeDatabase();

        dbManager.clearMissions();
        dbManager.clearAgents();
        dbManager.clearFacilities();
    }

    //Constructor and getConnection tests

    @Test
    @DisplayName("Default constructor creates a DatabaseManager")
    void testDefaultConstructor() {
        DatabaseManager dm = new DatabaseManager();
        assertNotNull(dm);
    }

    @Test
    @DisplayName("Path constructor creates a DatabaseManager")
    void testPathConstructor() {
        DatabaseManager dm = new DatabaseManager(":memory:");
        assertNotNull(dm);
    }

    @Test
    @DisplayName("getConnection returns valid open connection")
    void testGetConnection() throws SQLException {
        Connection connection = dbManager.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
    }

    @Test
    @DisplayName("getConnection reuses the same connection when called twice")
    void testGetConnectionReused() throws SQLException {
        Connection c1 = dbManager.getConnection();
        Connection c2 = dbManager.getConnection();
        assertSame(c1, c2);
    }

    // Initialize database tests

    @Test
    @DisplayName("initializeDatabase creates missions table")
    void testInitializeDatabaseCreatesMissionsTable() throws SQLException {
        assertEquals(0, dbManager.getMissionCount());
    }

    @Test
    @DisplayName("initializeDatabase creates briefs table")
    void testInitializeDatabaseCreatesBriefsTable() throws SQLException {
        assertEquals(0, dbManager.getBriefCount());
    }

    @Test
    @DisplayName("initializeDatabase creates facilities table")
    void testInitializeDatabaseCreatesFacilitiesTable() throws SQLException {
        assertEquals(0, dbManager.getFacilityCount());
    }

    @Test
    @DisplayName("initializeDatabase creates agents table")
    void testInitializeDatabaseCreatesAgentsTable() throws SQLException {
        assertEquals(0, dbManager.getAgentCount());
    }

    @Test
    @DisplayName("initializeDatabase is idempotent, no duplication of data")
    void testInitializeDatabaseIsIdempotent() throws SQLException, IOException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.initializeDatabase();
        assertEquals(1, dbManager.getFacilityCount());
    }

    // Mission tests

    @Test
    @DisplayName("addMission adds a record")
    void testAddMissionAddsARecord() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("Operation Sandtrap", "1970-11-03",
                "Bug the diplomatic pouch.", 1);

        assertEquals(1, dbManager.getMissionCount());
    }

    @Test
    @DisplayName("addMission stores all mission fields correctly")
    void testAddMissionStoresFields() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("Operation Sandtrap", "1970-11-03",
                "Bug the diplomatic pouch.", 1);

        Mission m = dbManager.getAllMissions().get(0);

        assertEquals("Operation Sandtrap", m.getTitle());
        assertEquals("1970-11-03", m.getDate());
        assertEquals("Bug the diplomatic pouch.", m.getDescription());
        assertEquals(1, m.getFacilityId());
    }

    @Test
    @DisplayName("getMissionCount returns correct count after multiple inserts")
    void testGetMissionCountMultiple() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addMission("T2", "1971-01-01", "D2", 1);
        dbManager.addMission("T3", "1972-01-01", "D3", 1);

        assertEquals(3, dbManager.getMissionCount());
    }

    @Test
    @DisplayName("getMissionById returns correct mission")
    void testGetMissionById() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("The Munich Lead", "1972-09-15",
                "Identify the cell.", 1);

        Mission m = dbManager.getMissionById(1);

        assertNotNull(m);
        assertEquals("The Munich Lead", m.getTitle());
        assertEquals("1972-09-15", m.getDate());
        assertEquals("Identify the cell.", m.getDescription());
    }

    @Test
    @DisplayName("getMissionById returns null for nonexisting ID")
    void testGetMissionByIdNonexisting() throws SQLException {
        assertNull(dbManager.getMissionById(999));
    }

    @Test
    @DisplayName("getAllMissions returns all records in order")
    void testGetAllMissions() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addMission("T2", "1971-01-01", "D2", 1);

        List<Mission> missions = dbManager.getAllMissions();

        assertEquals(2, missions.size());
        assertEquals("T1", missions.get(0).getTitle());
        assertEquals("T2", missions.get(1).getTitle());
    }

    @Test
    @DisplayName("getMissionsByFacility returns only missions for that facility")
    void testGetMissionsByFacility() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addFacility("Rose Garden", "RG");

        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addMission("T2", "1971-01-01", "D2", 2);
        dbManager.addMission("T3", "1972-01-01", "D3", 1);

        List<Mission> result = dbManager.getMissionsByFacility(1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getFacilityId() == 1));
    }

    @Test
    @DisplayName("updateMission updates all mission fields")
    void testUpdateMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addFacility("Rose Garden", "RG");

        dbManager.addMission("Old Title", "1970-01-01", "Old Description", 1);

        dbManager.updateMission(1, "New Title", "1971-02-02",
                "New Description", 2);

        Mission m = dbManager.getMissionById(1);

        assertNotNull(m);
        assertEquals("New Title", m.getTitle());
        assertEquals("1971-02-02", m.getDate());
        assertEquals("New Description", m.getDescription());
        assertEquals(2, m.getFacilityId());
    }

    @Test
    @DisplayName("clearMissions removes missions and related briefs")
    void testClearMissions() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addBrief("Brief 1", "1970-01-02", "Brief text.", 1);

        dbManager.clearMissions();

        assertEquals(0, dbManager.getMissionCount());
        assertEquals(0, dbManager.getBriefCount());
    }

    @Test
    @DisplayName("deleteMission removes mission and related briefs")
    void testDeleteMissionRemovesBriefs() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addBrief("Brief 1", "1970-01-02", "Brief text.", 1);

        dbManager.deleteMission(1);

        assertEquals(0, dbManager.getMissionCount());
        assertEquals(0, dbManager.getBriefCount());
    }

    @Test
    @DisplayName("deleteMission also removes mission_agents rows but leaves agents intact")
    void testDeleteMissionRemovesAgentLinks() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addAgentToMission(1, 1);

        dbManager.deleteMission(1);

        assertEquals(1, dbManager.getAgentCount());
        assertEquals(0, dbManager.getMissionCount());
        assertEquals(0, dbManager.getAgentsByMission(1).size());
    }

    // Brief tests

    @Test
    @DisplayName("addBrief adds a record")
    void testAddBriefAddsRecord() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addBrief("Brief 1", "1970-01-02", "Brief text.", 1);

        assertEquals(1, dbManager.getBriefCount());
    }

    @Test
    @DisplayName("addBrief stores all fields correctly")
    void testAddBriefStoresFields() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addBrief("Brief 1", "1970-01-02", "Brief text.", 1);

        Brief b = dbManager.getAllBriefs().get(0);

        assertEquals("Brief 1", b.getTitle());
        assertEquals("1970-01-02", b.getDate());
        assertEquals("Brief text.", b.getText());
        assertEquals(1, b.getMissionId());
    }

    @Test
    @DisplayName("getBriefById returns correct brief")
    void testGetBriefById() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addBrief("Brief 1", "1970-01-02", "Brief text.", 1);

        Brief b = dbManager.getBriefById(1);

        assertNotNull(b);
        assertEquals("Brief 1", b.getTitle());
    }

    @Test
    @DisplayName("getBriefById returns null for nonexisting ID")
    void testGetBriefByIdNonexisting() throws SQLException {
        assertNull(dbManager.getBriefById(999));
    }

    @Test
    @DisplayName("getAllBriefs returns all briefs in order")
    void testGetAllBriefs() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addBrief("Brief 1", "1970-01-02", "Text 1", 1);
        dbManager.addBrief("Brief 2", "1970-01-03", "Text 2", 1);

        List<Brief> briefs = dbManager.getAllBriefs();

        assertEquals(2, briefs.size());
        assertEquals("Brief 1", briefs.get(0).getTitle());
        assertEquals("Brief 2", briefs.get(1).getTitle());
    }

    @Test
    @DisplayName("getBriefsByMission returns only briefs for that mission")
    void testGetBriefsByMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addMission("T2", "1971-01-01", "D2", 1);

        dbManager.addBrief("Brief 1", "1970-01-02", "Text 1", 1);
        dbManager.addBrief("Brief 2", "1970-01-03", "Text 2", 1);
        dbManager.addBrief("Brief 3", "1971-01-02", "Text 3", 2);

        List<Brief> result = dbManager.getBriefsByMission(1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getMissionId() == 1));
    }

    @Test
    @DisplayName("mission can have zero briefs")
    void testMissionCanHaveZeroBriefs() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        List<Brief> result = dbManager.getBriefsByMission(1);

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("mission can have multiple briefs")
    void testMissionCanHaveMultipleBriefs() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addBrief("Brief 1", "1970-01-02", "Text 1", 1);
        dbManager.addBrief("Brief 2", "1970-01-03", "Text 2", 1);

        List<Brief> result = dbManager.getBriefsByMission(1);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("updateBrief updates all brief fields")
    void testUpdateBrief() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addMission("T2", "1971-01-01", "D2", 1);

        dbManager.addBrief("Old Brief", "1970-01-02", "Old text.", 1);

        dbManager.updateBrief(1, "New Brief", "1971-02-02", "New text.", 2);

        Brief b = dbManager.getBriefById(1);

        assertNotNull(b);
        assertEquals("New Brief", b.getTitle());
        assertEquals("1971-02-02", b.getDate());
        assertEquals("New text.", b.getText());
        assertEquals(2, b.getMissionId());
    }

    @Test
    @DisplayName("deleteBrief removes only that brief")
    void testDeleteBrief() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addBrief("Brief 1", "1970-01-02", "Text 1", 1);
        dbManager.addBrief("Brief 2", "1970-01-03", "Text 2", 1);

        dbManager.deleteBrief(1);

        assertEquals(1, dbManager.getBriefCount());
        assertNull(dbManager.getBriefById(1));
        assertNotNull(dbManager.getBriefById(2));
    }



    // Facility tests

    @Test
    @DisplayName("addFacility adds record")
    void testAddFacility() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        assertEquals(1, dbManager.getFacilityCount());
    }

    @Test
    @DisplayName("addFacility stores name and abbreviation correctly")
    void testAddFacilityNameAndAbbreviation() throws SQLException {
        dbManager.addFacility("Rose Garden", "RG");

        Facility f = dbManager.getAllFacilities().get(0);

        assertEquals("Rose Garden", f.getName());
        assertEquals("RG", f.getAbbreviation());
    }

    @Test
    @DisplayName("getFacilityById returns correct facility")
    void testGetFacilityById() throws SQLException {
        dbManager.addFacility("Echo Point", "EP");

        Facility f = dbManager.getFacilityById(1);

        assertNotNull(f);
        assertEquals("Echo Point", f.getName());
        assertEquals("EP", f.getAbbreviation());
    }

    @Test
    @DisplayName("getFacilityById returns null for nonexistent id")
    void testGetFacilityByIdNotFound() throws SQLException {
        assertNull(dbManager.getFacilityById(999));
    }

    @Test
    @DisplayName("getAllFacilities returns all records in order")
    void testGetAllFacilities() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addFacility("Rose Garden", "RG");

        List<Facility> facilities = dbManager.getAllFacilities();

        assertEquals(2, facilities.size());
        assertEquals("Blackwood Annex", facilities.get(0).getName());
        assertEquals("Rose Garden", facilities.get(1).getName());
    }

    @Test
    @DisplayName("getFacilityCount returns correct count")
    void testGetFacilityCount() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addFacility("Rose Garden", "RG");

        assertEquals(2, dbManager.getFacilityCount());
    }

    @Test
    @DisplayName("clearFacilities removes all records")
    void testClearFacilities() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.clearFacilities();

        assertEquals(0, dbManager.getFacilityCount());
    }

    @Test
    @DisplayName("deleteFacility removes facility when nothing references it")
    void testDeleteFacility() throws SQLException {
        dbManager.addFacility("Glass House", "GA");

        dbManager.deleteFacility(1);

        assertEquals(0, dbManager.getFacilityCount());
    }

    @Test
    @DisplayName("deleteFacility is blocked when agents are assigned to it")
    void testDeleteFacilityBlockedByAgents() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);

        dbManager.deleteFacility(1);

        assertEquals(1, dbManager.getFacilityCount());
    }

    @Test
    @DisplayName("deleteFacility is blocked when missions are assigned to it")
    void testDeleteFacilityBlockedByMissions() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.deleteFacility(1);

        assertEquals(1, dbManager.getFacilityCount());
    }

    // Mission-agent relationship tests

    @Test
    @DisplayName("addAgentToMission links agent to mission")
    void testAddAgentToMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addAgentToMission(1, 1);

        List<Agent> agents = dbManager.getAgentsByMission(1);

        assertEquals(1, agents.size());
        assertEquals("Vanguard", agents.get(0).getName());
    }

    @Test
    @DisplayName("getAgentsByMission returns all agents on a mission")
    void testGetAgentsByMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addAgent("Ghost", "November 22, 1992", "", "", 1);
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addAgentToMission(1, 1);
        dbManager.addAgentToMission(1, 2);

        assertEquals(2, dbManager.getAgentsByMission(1).size());
    }

    @Test
    @DisplayName("agent can be assigned to multiple missions")
    void testAgentCanBeAssignedToMultipleMissions() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addMission("T2", "1971-01-01", "D2", 1);

        dbManager.addAgentToMission(1, 1);
        dbManager.addAgentToMission(2, 1);

        assertEquals(1, dbManager.getAgentsByMission(1).size());
        assertEquals(1, dbManager.getAgentsByMission(2).size());
    }

    @Test
    @DisplayName("agents from different facilities can be assigned to same mission")
    void testAgentsFromDifferentFacilitiesCanJoinSameMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addFacility("Rose Garden", "RG");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addAgent("Ghost", "November 22, 1992", "", "", 2);

        dbManager.addMission("Joint Operation", "1975-05-05",
                "Joint operation with multiple facilities.", 1);

        dbManager.addAgentToMission(1, 1);
        dbManager.addAgentToMission(1, 2);

        List<Agent> agents = dbManager.getAgentsByMission(1);

        assertEquals(2, agents.size());
        assertTrue(agents.stream().anyMatch(a -> a.getFacilityId() == 1));
        assertTrue(agents.stream().anyMatch(a -> a.getFacilityId() == 2));
    }

    @Test
    @DisplayName("removeAgentFromMission unlinks when others stay")
    void testRemoveAgentFromMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addAgent("Ghost", "November 22, 1992", "", "", 1);

        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addAgentToMission(1, 1);
        dbManager.addAgentToMission(1, 2);

        dbManager.removeAgentFromMission(1, 1);

        List<Agent> agents = dbManager.getAgentsByMission(1);

        assertEquals(1, agents.size());
        assertEquals("Ghost", agents.get(0).getName());
    }

    @Test
    @DisplayName("removeAgentFromMission allows mission to have zero agents")
    void testRemoveAgentFromMissionAllowsZeroAgents() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addMission("T1", "1970-01-01", "D1", 1);

        dbManager.addAgentToMission(1, 1);
        dbManager.removeAgentFromMission(1, 1);

        assertEquals(0, dbManager.getAgentsByMission(1).size());
    }

    // Agent tests

    @Test
    @DisplayName("addAgent adds a record")
    void testAddAgentAddsRecord() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);

        assertEquals(1, dbManager.getAgentCount());
    }

    @Test
    @DisplayName("addAgent stores fields correctly")
    void testAddAgentStoresFields() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Ghost", "November 22, 1992",
                "2020-01-01", "Deceased", 1);

        Agent a = dbManager.getAllAgents().get(0);

        assertEquals("Ghost", a.getName());
        assertEquals("November 22, 1992", a.getDateOfBirth());
        assertEquals("2020-01-01", a.getDateOfDeath());
        assertEquals("Deceased", a.getNotes());
        assertEquals(1, a.getFacilityId());
    }

    @Test
    @DisplayName("getAgentCount returns correct count")
    void testGetAgentCount() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addAgent("Ghost", "November 22, 1992", "", "", 1);

        assertEquals(2, dbManager.getAgentCount());
    }

    @Test
    @DisplayName("getAgentById returns correct agent")
    void testGetAgentById() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Mirage", "March 03, 1988", "", "", 1);

        Agent a = dbManager.getAgentById(1);

        assertNotNull(a);
        assertEquals("Mirage", a.getName());
    }

    @Test
    @DisplayName("getAgentById returns null for nonexisting id")
    void testGetAgentByIdNotFound() throws SQLException {
        assertNull(dbManager.getAgentById(988));
    }

    @Test
    @DisplayName("getAllAgents returns all records in order")
    void testGetAllAgents() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addAgent("Ghost", "November 22, 1992", "", "", 1);

        List<Agent> agents = dbManager.getAllAgents();

        assertEquals(2, agents.size());
        assertEquals("Vanguard", agents.get(0).getName());
        assertEquals("Ghost", agents.get(1).getName());
    }

    @Test
    @DisplayName("getAgentsByFacility returns only agents for that facility")
    void testGetAgentsByFacility() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addFacility("Rose Garden", "RG");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addAgent("Ghost", "November 22, 1992", "", "", 2);
        dbManager.addAgent("Mirage", "March 03, 1988", "", "", 1);

        List<Agent> result = dbManager.getAgentsByFacility(1);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getFacilityId() == 1));
    }

    @Test
    @DisplayName("clearAgents removes all records")
    void testClearAgents() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);

        dbManager.clearAgents();

        assertEquals(0, dbManager.getAgentCount());
    }

    @Test
    @DisplayName("deleteAgent removes agent when not on any mission")
    void testDeleteAgent() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");
        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);

        dbManager.deleteAgent(1);

        assertEquals(0, dbManager.getAgentCount());
    }

    @Test
    @DisplayName("deleteAgent is blocked when agent is on an active mission")
    void testDeleteAgentBlockedByMission() throws SQLException {
        dbManager.addFacility("Blackwood Annex", "BA");

        dbManager.addAgent("Vanguard", "June 14, 1985", "", "", 1);
        dbManager.addMission("T1", "1970-01-01", "D1", 1);
        dbManager.addAgentToMission(1, 1);

        dbManager.deleteAgent(1);

        assertEquals(1, dbManager.getAgentCount());
    }

    // Audit log tests

    @Test
    @DisplayName("logEvent stores audit log entry")
    void testLogEventStoresAuditLogEntry() throws SQLException {
        dbManager.logEvent("read", "brief", 1, "jonah");

        List<String> logs = dbManager.getAuditLog();

        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("read"));
        assertTrue(logs.get(0).contains("brief"));
        assertTrue(logs.get(0).contains("jonah"));
    }


}//end of DBManager test