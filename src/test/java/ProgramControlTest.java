import controller.ProgramControl;
import model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramControlTest {

    // -------------------------
    // Fake Repositories
    // -------------------------

    static class FakeBriefRepository implements BriefRepository {
        private final List<Brief> briefs = new ArrayList<>();

        public FakeBriefRepository() {
            briefs.add(new Brief(1, "Alpha Brief", "2024-01-01", "Alpha brief.", 1));
            briefs.add(new Brief(2, "Beta Brief", "2024-02-01", "Beta brief.", 2));
        }

        @Override
        public void addBrief(String title, String date, String text, int missionId) {
            int newId = briefs.size() + 1;
            briefs.add(new Brief(newId, title, date, text, missionId));
        }

        @Override
        public Brief getBriefById(int id) {
            for (Brief brief : briefs) {
                if (brief.getId() == id) {
                    return brief;
                }
            }

            return null;
        }

        @Override
        public List<Brief> getAllBriefs() {
            return briefs;
        }

        @Override
        public List<Brief> getBriefsByMission(int missionId) {
            ArrayList<Brief> result = new ArrayList<>();

            for (Brief brief : briefs) {
                if (brief.getMissionId() == missionId) {
                    result.add(brief);
                }
            }

            return result;
        }

        @Override
        public void updateBrief(int id, String title, String date, String text, int missionId) {
        }

        @Override
        public void deleteBrief(int id) {
        }
    }

    static class FakeMissionRepository implements MissionRepository {
        private final List<Mission> missions = new ArrayList<>();

        public FakeMissionRepository() {
            missions.add(new Mission(1, "Operation Alpha", "2024-01-01", "Alpha description.", 1));
            missions.add(new Mission(2, "Operation Beta", "2024-02-01", "Beta description.", 1));
        }

        @Override
        public List<Mission> getAllMissions() {
            return missions;
        }

        @Override
        public Mission getMissionById(int id) {
            for (Mission mission : missions) {
                if (mission.getId() == id) {
                    return mission;
                }
            }

            return null;
        }

        @Override
        public List<Mission> getMissionsByFacility(int facilityId) {
            ArrayList<Mission> result = new ArrayList<>();

            for (Mission mission : missions) {
                if (mission.getFacilityId() == facilityId) {
                    result.add(mission);
                }
            }

            return result;
        }

        @Override
        public List<Agent> getAgentsByMission(int missionId) {
            return new ArrayList<>();
        }

        @Override
        public void addMission(String title, String date, String description, int facilityId) {
            int newId = missions.size() + 1;
            missions.add(new Mission(newId, title, date, description, facilityId));
        }

        @Override
        public void updateMission(int id, String title, String date, String description, int facilityId) {
        }

        @Override
        public void addAgentToMission(int missionId, int agentId) {
        }

        @Override
        public void removeAgentFromMission(int missionId, int agentId) {
        }

        @Override
        public void deleteMission(int id) {
        }
    }

    static class FakeAgentRepository implements AgentRepository {
        private final List<Agent> agents = new ArrayList<>();

        public FakeAgentRepository() {
            agents.add(new Agent(1, "Agent Smith", "1980-01-01", "", "Field agent.", 1));
            agents.add(new Agent(2, "Agent Jones", "1985-02-02", "", "Analyst.", 1));
        }

        @Override
        public List<Agent> getAllAgents() {
            return agents;
        }

        @Override
        public Agent getAgentById(int id) {
            for (Agent agent : agents) {
                if (agent.getId() == id) {
                    return agent;
                }
            }

            return null;
        }

        @Override
        public List<Agent> getAgentsByFacility(int facilityId) {
            ArrayList<Agent> result = new ArrayList<>();

            for (Agent agent : agents) {
                if (agent.getFacilityId() == facilityId) {
                    result.add(agent);
                }
            }

            return result;
        }

        @Override
        public void addAgent(String name, String dateOfBirth, String dateOfDeath, String notes, int facilityId) {
            int newId = agents.size() + 1;
            agents.add(new Agent(newId, name, dateOfBirth, dateOfDeath, notes, facilityId));
        }

        @Override
        public void deleteAgent(int id) {
        }
    }

    static class FakeFacilityRepository implements FacilityRepository {
        private final List<Facility> facilities = new ArrayList<>();

        public FakeFacilityRepository() {
            facilities.add(new Facility(1, "Alpha Facility", "ALP"));
            facilities.add(new Facility(2, "Beta Facility", "BET"));
        }

        @Override
        public List<Facility> getAllFacilities() {
            return facilities;
        }

        @Override
        public Facility getFacilityById(int id) {
            for (Facility facility : facilities) {
                if (facility.getId() == id) {
                    return facility;
                }
            }

            return null;
        }

        @Override
        public void addFacility(String name, String abbreviation) {
            int newId = facilities.size() + 1;
            facilities.add(new Facility(newId, name, abbreviation));
        }

        @Override
        public void deleteFacility(int id) {
        }
    }

    static class FakeAuditLogRepository implements AuditLogRepository {
        private final List<String> logs = new ArrayList<>();

        public FakeAuditLogRepository() {
            logs.add("[2026-06-07 12:00:00] read brief (id:1) by unknown");
            logs.add("[2026-06-07 12:01:00] create agent (id:-1) by unknown");
        }

        @Override
        public void logEvent(String eventType, String contentType, int contentId, String username) {
            logs.add("[" + eventType + "] " + contentType + " (id:" + contentId + ") by " + username);
        }

        @Override
        public List<String> getAuditLog() {
            return logs;
        }
    }

    static class EmptyBriefRepository implements BriefRepository {
        @Override
        public void addBrief(String title, String date, String text, int missionId) {
        }

        @Override
        public Brief getBriefById(int id) {
            return null;
        }

        @Override
        public List<Brief> getAllBriefs() {
            return new ArrayList<>();
        }

        @Override
        public List<Brief> getBriefsByMission(int missionId) {
            return new ArrayList<>();
        }

        @Override
        public void updateBrief(int id, String title, String date, String text, int missionId) {
        }

        @Override
        public void deleteBrief(int id) {
        }
    }

    static class EmptyMissionRepository implements MissionRepository {
        @Override
        public List<Mission> getAllMissions() {
            return new ArrayList<>();
        }

        @Override
        public Mission getMissionById(int id) {
            return null;
        }

        @Override
        public List<Mission> getMissionsByFacility(int facilityId) {
            return new ArrayList<>();
        }

        @Override
        public List<Agent> getAgentsByMission(int missionId) {
            return new ArrayList<>();
        }

        @Override
        public void addMission(String title, String date, String description, int facilityId) {
        }

        @Override
        public void updateMission(int id, String title, String date, String description, int facilityId) {
        }

        @Override
        public void addAgentToMission(int missionId, int agentId) {
        }

        @Override
        public void removeAgentFromMission(int missionId, int agentId) {
        }

        @Override
        public void deleteMission(int id) {
        }
    }

    static class EmptyAgentRepository implements AgentRepository {
        @Override
        public List<Agent> getAllAgents() {
            return new ArrayList<>();
        }

        @Override
        public Agent getAgentById(int id) {
            return null;
        }

        @Override
        public List<Agent> getAgentsByFacility(int facilityId) {
            return new ArrayList<>();
        }

        @Override
        public void addAgent(String name, String dateOfBirth, String dateOfDeath, String notes, int facilityId) {
        }

        @Override
        public void deleteAgent(int id) {
        }
    }

    static class EmptyFacilityRepository implements FacilityRepository {
        @Override
        public List<Facility> getAllFacilities() {
            return new ArrayList<>();
        }

        @Override
        public Facility getFacilityById(int id) {
            return null;
        }

        @Override
        public void addFacility(String name, String abbreviation) {
        }

        @Override
        public void deleteFacility(int id) {
        }
    }

    // -------------------------
    // Setup
    // -------------------------

    @BeforeEach
    void setUp() {
        ProgramControl.setBriefRepository(new FakeBriefRepository());
        ProgramControl.setMissionRepository(new FakeMissionRepository());
        ProgramControl.setAgentRepository(new FakeAgentRepository());
        ProgramControl.setFacilityRepository(new FakeFacilityRepository());
        ProgramControl.setAuditLogRepository(new FakeAuditLogRepository());
        ProgramControl.setCurrentUsername("unknown");
    }

    // -------------------------
    // Brief Tests
    // -------------------------

    @Test
    void listBriefsShowsNumberedBriefTitles() {
        String result = ProgramControl.listBriefs();

        assertTrue(result.contains("1. Alpha Brief"));
        assertTrue(result.contains("2. Beta Brief"));
    }

    @Test
    void displayBriefShowsSelectedBriefInformation() {
        String result = ProgramControl.displayBrief("1");

        assertTrue(result.contains("Alpha Brief"));
        assertTrue(result.contains("2024-01-01"));
        assertTrue(result.contains("Alpha brief."));
        assertTrue(result.contains("Mission ID: 1"));
    }

    @Test
    void displayBriefRejectsInvalidNumber() {
        String result = ProgramControl.displayBrief("99");

        assertEquals("Error: Invalid brief number: 99", result);
    }

    @Test
    void displayBriefRejectsNonNumber() {
        String result = ProgramControl.displayBrief("abc");

        assertEquals("Error: Please enter a valid number.", result);
    }

    @Test
    void addBriefAddsNewBrief() {
        String result = ProgramControl.addBrief("Gamma Brief", "2024-03-01", "Gamma brief.", "1");

        assertEquals("Brief added successfully.", result);

        String list = ProgramControl.listBriefs();
        assertTrue(list.contains("Gamma Brief"));
    }

    @Test
    void addBriefRejectsNonNumericMissionId() {
        String result = ProgramControl.addBrief("Bad Brief", "2024-03-01", "Text", "abc");

        assertEquals("Error: Mission ID must be a number.", result);
    }

    @Test
    void addBriefRejectsBlankTitle() {
        String result = ProgramControl.addBrief("", "2024-03-01", "Gamma brief.", "1");

        assertEquals("Error: Brief fields cannot be blank.", result);
    }

    @Test
    void addBriefRejectsBlankMissionId() {
        String result = ProgramControl.addBrief("Gamma Brief", "2024-03-01", "Gamma brief.", "");

        assertEquals("Error: Brief fields cannot be blank.", result);
    }

    @Test
    void listBriefsReturnsMessageWhenEmpty() {
        ProgramControl.setBriefRepository(new EmptyBriefRepository());

        String result = ProgramControl.listBriefs();

        assertEquals("No briefs available.", result);
    }

    @Test
    void displayBriefLogsCurrentUsername() {
        ProgramControl.setCurrentUsername("agent");

        ProgramControl.displayBrief("1");

        String result = ProgramControl.listAuditLogs();

        assertTrue(result.contains("agent"));
        assertTrue(result.contains("read"));
        assertTrue(result.contains("brief"));
    }

    // -------------------------
    // Mission Tests
    // -------------------------

    @Test
    void listMissionsShowsNumberedMissionTitles() {
        String result = ProgramControl.listMissions();

        assertTrue(result.contains("1. Operation Alpha"));
        assertTrue(result.contains("2. Operation Beta"));
    }

    @Test
    void displayMissionShowsSelectedMissionInformation() {
        String result = ProgramControl.displayMission("1");

        assertTrue(result.contains("Operation Alpha"));
        assertTrue(result.contains("2024-01-01"));
        assertTrue(result.contains("Alpha description."));
        assertTrue(result.contains("Facility ID: 1"));
    }

    @Test
    void displayMissionRejectsInvalidNumber() {
        String result = ProgramControl.displayMission("99");

        assertEquals("Error: Invalid mission number: 99", result);
    }

    @Test
    void displayMissionRejectsNonNumber() {
        String result = ProgramControl.displayMission("abc");

        assertEquals("Error: Please enter a valid number.", result);
    }

    @Test
    void addMissionAddsNewMission() {
        String result = ProgramControl.addMission("Operation Gamma", "2024-03-01", "Gamma description.", "1");

        assertEquals("Mission added successfully.", result);

        String list = ProgramControl.listMissions();
        assertTrue(list.contains("Operation Gamma"));
    }

    @Test
    void addMissionRejectsNonNumericFacilityId() {
        String result = ProgramControl.addMission("Bad Mission", "2024-03-01", "Bad description.", "abc");

        assertEquals("Error: Facility ID must be a number.", result);
    }

    @Test
    void addMissionRejectsBlankTitle() {
        String result = ProgramControl.addMission("", "2024-03-01", "Gamma description.", "1");

        assertEquals("Error: Mission fields cannot be blank.", result);
    }

    @Test
    void listMissionsReturnsMessageWhenEmpty() {
        ProgramControl.setMissionRepository(new EmptyMissionRepository());

        String result = ProgramControl.listMissions();

        assertEquals("No missions available.", result);
    }

    // -------------------------
    // Agent Tests
    // -------------------------

    @Test
    void listAgentsShowsNumberedAgentNames() {
        String result = ProgramControl.listAgents();

        assertTrue(result.contains("1. Agent Smith"));
        assertTrue(result.contains("2. Agent Jones"));
    }

    @Test
    void displayAgentShowsSelectedAgentInformation() {
        String result = ProgramControl.displayAgent("1");

        assertTrue(result.contains("Agent Smith"));
        assertTrue(result.contains("1980-01-01"));
        assertTrue(result.contains("Field agent."));
        assertTrue(result.contains("Facility ID: 1"));
    }

    @Test
    void displayAgentRejectsInvalidNumber() {
        String result = ProgramControl.displayAgent("99");

        assertEquals("Error: Invalid agent number: 99", result);
    }

    @Test
    void displayAgentRejectsNonNumber() {
        String result = ProgramControl.displayAgent("abc");

        assertEquals("Error: Please enter a valid number.", result);
    }

    @Test
    void addAgentAddsNewAgent() {
        String result = ProgramControl.addAgent(
                "Agent Brown",
                "1990-03-03",
                "",
                "New agent.",
                "1"
        );

        assertEquals("Agent added successfully.", result);

        String list = ProgramControl.listAgents();
        assertTrue(list.contains("Agent Brown"));
    }

    @Test
    void addAgentRejectsNonNumericFacilityId() {
        String result = ProgramControl.addAgent(
                "Agent Bad",
                "1990-03-03",
                "",
                "Bad facility.",
                "abc"
        );

        assertEquals("Error: Facility ID must be a number.", result);
    }

    @Test
    void addAgentRejectsBlankName() {
        String result = ProgramControl.addAgent(
                "",
                "1990-03-03",
                "",
                "New agent.",
                "1"
        );

        assertEquals("Error: Agent name, date of birth, and facility ID cannot be blank.", result);
    }

    @Test
    void addAgentRejectsBlankFacilityId() {
        String result = ProgramControl.addAgent(
                "Agent Brown",
                "1990-03-03",
                "",
                "New agent.",
                ""
        );

        assertEquals("Error: Agent name, date of birth, and facility ID cannot be blank.", result);
    }

    @Test
    void listAgentsReturnsMessageWhenEmpty() {
        ProgramControl.setAgentRepository(new EmptyAgentRepository());

        String result = ProgramControl.listAgents();

        assertEquals("No agents available.", result);
    }

    // -------------------------
    // Facility Tests
    // -------------------------

    @Test
    void listFacilitiesShowsNumberedFacilities() {
        String result = ProgramControl.listFacilities();

        assertTrue(result.contains("1. Alpha Facility"));
        assertTrue(result.contains("ALP"));
        assertTrue(result.contains("2. Beta Facility"));
        assertTrue(result.contains("BET"));
    }

    @Test
    void displayFacilityShowsSelectedFacilityInformation() {
        String result = ProgramControl.displayFacility("1");

        assertTrue(result.contains("Alpha Facility"));
        assertTrue(result.contains("ALP"));
    }

    @Test
    void displayFacilityRejectsInvalidNumber() {
        String result = ProgramControl.displayFacility("99");

        assertEquals("Error: Invalid facility number: 99", result);
    }

    @Test
    void displayFacilityRejectsNonNumber() {
        String result = ProgramControl.displayFacility("abc");

        assertEquals("Error: Please enter a valid number.", result);
    }

    @Test
    void addFacilityAddsNewFacility() {
        String result = ProgramControl.addFacility("Gamma Facility", "GAM");

        assertEquals("Facility added successfully.", result);

        String list = ProgramControl.listFacilities();
        assertTrue(list.contains("Gamma Facility"));
        assertTrue(list.contains("GAM"));
    }

    @Test
    void addFacilityRejectsBlankName() {
        String result = ProgramControl.addFacility("", "GAM");

        assertEquals("Error: Facility name and abbreviation cannot be blank.", result);
    }

    @Test
    void addFacilityRejectsBlankAbbreviation() {
        String result = ProgramControl.addFacility("Gamma Facility", "");

        assertEquals("Error: Facility name and abbreviation cannot be blank.", result);
    }

    @Test
    void listFacilitiesReturnsMessageWhenEmpty() {
        ProgramControl.setFacilityRepository(new EmptyFacilityRepository());

        String result = ProgramControl.listFacilities();

        assertEquals("No facilities available.", result);
    }

    // -------------------------
    // Audit Log Tests
    // -------------------------

    @Test
    void listAuditLogsShowsAuditEntries() {
        String result = ProgramControl.listAuditLogs();

        assertTrue(result.contains("read brief"));
        assertTrue(result.contains("create agent"));
        assertTrue(result.contains("unknown"));
    }
}