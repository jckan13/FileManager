import static org.junit.jupiter.api.Assertions.*;

import controller.ProgramControl;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.UserInterface;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class UserInterfaceTest {

    static class FakeLoginService implements LoginService {
        private final Map<String, String> credentials = new LinkedHashMap<>();

        public FakeLoginService() {
            credentials.put("agent", "Abcde");
            credentials.put("newuser", "");
        }

        @Override
        public boolean credentialsExist() {
            return !credentials.isEmpty();
        }

        @Override
        public boolean isValidUsername(String username) {
            if (username == null) {
                return false;
            }

            return username.matches("[a-zA-Z0-9_\\-]{3,}");
        }

        @Override
        public boolean isValidPassword(String password) {
            if (password == null || password.length() < 5) {
                return false;
            }

            boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
            boolean hasLower = password.chars().anyMatch(Character::isLowerCase);

            return hasUpper && hasLower;
        }

        @Override
        public void createCredentials(String username, String password) {
            credentials.put(username, password);
        }

        @Override
        public boolean login(String username, String password) {
            return credentials.containsKey(username) &&
                    credentials.get(username).equals(password);
        }

        @Override
        public void changePassword(String username, String currentPassword,
                                   String newPassword, String confirmPassword) {
            credentials.put(username, newPassword);
        }

        @Override
        public boolean usernameExists(String username) {
            return credentials.containsKey(username);
        }

        @Override
        public boolean hasNoPassword(String username) {
            return credentials.containsKey(username) &&
                    credentials.get(username).isEmpty();
        }

        @Override
        public void addUser(String username) {
            if (!isValidUsername(username)) {
                throw new IllegalArgumentException("Username must be at least 3 characters and can only contain letters, numbers, hyphens, and underscores.");
            }

            if (credentials.containsKey(username)) {
                throw new IllegalArgumentException("Username already exists.");
            }

            credentials.put(username, "");
        }
    }

    static class FakeMissionRepository implements MissionRepository {
        private final List<Mission> missions = new ArrayList<>();

        public FakeMissionRepository() {
            missions.add(new Mission(1, "Operation Alpha", "2024-01-01", "Alpha brief.", 1));
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
        public void addAgentToMission(int missionId, int agentId) {
        }

        @Override
        public void removeAgentFromMission(int missionId, int agentId) {
        }

        @Override
        public void deleteMission(int id) {
        }

        @Override
        public void updateMission(int id, String title, String date, String description, int facilityId) {

        }
    }

    static class FakeBriefRepository implements BriefRepository {
        private final List<Brief> briefs = new ArrayList<>();

        public FakeBriefRepository() {
            briefs.add(new Brief(1, "Operation Alpha", "2024-01-01", "Alpha brief.", 1));
        }

        @Override
        public List<Brief> getAllBriefs() {
            return briefs;
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
        public void updateBrief(int id, String title, String date, String text, int missionId) throws SQLException {

        }

        @Override
        public void deleteBrief(int id) throws SQLException {

        }

        @Override
        public void addBrief(String title, String date, String text, int missionId) {
            int newId = briefs.size() + 1;
            briefs.add(new Brief(newId, title, date, text, missionId));
        }
    }


    static class FakeAgentRepository implements AgentRepository {
        private final List<Agent> agents = new ArrayList<>();

        public FakeAgentRepository() {
            agents.add(new Agent(1, "Agent Smith", "1980-01-01", "", "Field agent.", 1));
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
        public void addAgent(String name, String dateOfBirth, String dateOfDeath,
                             String notes, int facilityId) {
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

        @Override
        public void logEvent(String eventType, String contentType, int contentId, String username) {
            logs.add(eventType + " " + contentType + " (id:" + contentId + ") by " + username);
        }

        @Override
        public List<String> getAuditLog() {
            return logs;
        }
    }

    @BeforeEach
    void setUp() {
        ProgramControl.setBriefRepository(new FakeBriefRepository());
        ProgramControl.setMissionRepository(new FakeMissionRepository());
        ProgramControl.setAgentRepository(new FakeAgentRepository());
        ProgramControl.setFacilityRepository(new FakeFacilityRepository());
        ProgramControl.setAuditLogRepository(new FakeAuditLogRepository());
        ProgramControl.setCurrentUsername("unknown");
    }

    @Test
    void userCanLoginOpenBriefsListBriefsAndExit() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "1\n" +   // Briefs
                        "1\n" +   // List briefs
                        "0\n" +   // Return without reading
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Logged in successfully."));
        assertTrue(printed.contains("Top Secret System"));
        assertTrue(printed.contains("Briefs Menu"));
        assertTrue(printed.contains("Available Briefs"));
        assertTrue(printed.contains("Operation Alpha"));
        assertTrue(printed.contains("Exiting Top Secret System."));
    }

    @Test
    void userCanListBriefsAndReadBriefImmediately() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "1\n" +   // Briefs
                        "1\n" +   // List briefs
                        "1\n" +   // Read brief 1
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Available Briefs"));
        assertTrue(printed.contains("Title: Operation Alpha"));
        assertTrue(printed.contains("Alpha brief."));
    }

    @Test
    void userCanReadBriefThroughReadOption() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "1\n" +   // Briefs
                        "2\n" +   // Read a brief
                        "1\n" +   // Brief number
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Title: Operation Alpha"));
        assertTrue(printed.contains("Alpha brief."));
    }

    @Test
    void userCanAddBriefAndThenSeeItInList() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "1\n" +               // Briefs
                        "3\n" +               // Add brief
                        "Operation Beta\n" +
                        "2024-02-01\n" +
                        "Beta brief.\n" +
                        "1\n" +
                        "1\n" +               // List briefs
                        "0\n" +               // Return without reading
                        "4\n" +               // Back
                        "7\n"                 // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Brief added successfully."));
        assertTrue(printed.contains("Operation Beta"));
    }

    @Test
    void userCanOpenAgentsListAgentsAndExit() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "2\n" +   // Agents
                        "1\n" +   // List agents
                        "0\n" +   // Return without reading
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Agents Menu"));
        assertTrue(printed.contains("Available Agents"));
        assertTrue(printed.contains("Agent Smith"));
    }

    @Test
    void userCanListAgentsAndReadAgentImmediately() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "2\n" +   // Agents
                        "1\n" +   // List agents
                        "1\n" +   // Read agent 1
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Available Agents"));
        assertTrue(printed.contains("Name: Agent Smith"));
        assertTrue(printed.contains("Field agent."));
    }

    @Test
    void userCanAddAgentAndThenSeeItInList() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "2\n" +              // Agents
                        "3\n" +              // Add agent
                        "Agent Brown\n" +
                        "1990-03-03\n" +
                        "\n" +
                        "New agent notes.\n" +
                        "1\n" +
                        "1\n" +              // List agents
                        "0\n" +              // Return without reading
                        "4\n" +              // Back
                        "7\n"                // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Agent added successfully."));
        assertTrue(printed.contains("Agent Brown"));
    }

    @Test
    void userCanOpenFacilitiesListFacilitiesAndExit() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "3\n" +   // Facilities
                        "1\n" +   // List facilities
                        "0\n" +   // Return without reading
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Facilities Menu"));
        assertTrue(printed.contains("Available Facilities"));
        assertTrue(printed.contains("Alpha Facility"));
    }

    @Test
    void userCanListFacilitiesAndReadFacilityImmediately() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "3\n" +   // Facilities
                        "1\n" +   // List facilities
                        "1\n" +   // Read facility 1
                        "4\n" +   // Back
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Available Facilities"));
        assertTrue(printed.contains("Name: Alpha Facility"));
        assertTrue(printed.contains("ALP"));
    }

    @Test
    void userCanAddFacilityAndThenSeeItInList() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "3\n" +              // Facilities
                        "3\n" +              // Add facility
                        "Beta Facility\n" +
                        "BET\n" +
                        "1\n" +              // List facilities
                        "0\n" +              // Return without reading
                        "4\n" +              // Back
                        "7\n"                // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Facility added successfully."));
        assertTrue(printed.contains("Beta Facility"));
        assertTrue(printed.contains("BET"));
    }

    @Test
    void userCanReviewAuditLogOptionAfterReadingBrief() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "1\n" +   // Briefs
                        "2\n" +   // Read brief
                        "1\n" +   // Brief number
                        "4\n" +   // Back
                        "5\n" +   // Review Audit Log
                        "7\n"     // Exit
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Audit Log"));
        assertTrue(printed.contains("read brief"));
        assertTrue(printed.contains("agent"));
    }


    @Test
    void newUserWithoutPasswordCanCreatePasswordOnFirstLogin() {
        Scanner scanner = new Scanner(
                "newuser\n" +
                        "Password\n" +
                        "Password\n"
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("This user does not have a password yet."));
        assertTrue(printed.contains("Password created successfully."));
    }

    @Test
    void newUserPasswordCreationRejectsMismatch() {
        Scanner scanner = new Scanner(
                "newuser\n" +
                        "Password\n" +
                        "Different\n"
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("This user does not have a password yet."));
        assertTrue(printed.contains("Error: New passwords do not match."));
    }

    @Test
    void invalidMainMenuOptionShowsError() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "wrong\n" +
                        "7\n"
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Invalid option."));
    }

    @Test
    void invalidBriefsMenuOptionShowsError() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "1\n" +
                        "wrong\n" +
                        "4\n" +
                        "7\n"
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Briefs Menu"));
        assertTrue(printed.contains("Invalid option."));
    }

    @Test
    void invalidAgentsMenuOptionShowsError() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "2\n" +
                        "wrong\n" +
                        "4\n" +
                        "7\n"
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Agents Menu"));
        assertTrue(printed.contains("Invalid option."));
    }

    @Test
    void invalidFacilitiesMenuOptionShowsError() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "3\n" +
                        "wrong\n" +
                        "4\n" +
                        "7\n"
        );

        String printed = runWithCapturedOutput(scanner);

        assertTrue(printed.contains("Facilities Menu"));
        assertTrue(printed.contains("Invalid option."));
    }

    private String runWithCapturedOutput(Scanner scanner) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        System.setOut(new PrintStream(output));

        UserInterface.start(new FakeLoginService(), scanner);

        System.setOut(originalOut);

        return output.toString();
    }
    @Test
    void userCanOpenMissionsListMissionsAndExit() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "4\n" +   // Missions
                        "1\n" +   // List missions
                        "0\n" +   // Return without reading
                        "4\n" +   // Back
                        "7\n"     // Exit
        );
        String printed = runWithCapturedOutput(scanner);
        assertTrue(printed.contains("Missions Menu"));
        assertTrue(printed.contains("Available Missions"));
        assertTrue(printed.contains("Operation Alpha"));
    }
    @Test
    void userCanListMissionsAndReadMissionImmediately() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "4\n" +   // Missions
                        "1\n" +   // List missions
                        "1\n" +   // Read mission 1
                        "4\n" +   // Back
                        "7\n"     // Exit
        );
        String printed = runWithCapturedOutput(scanner);
        assertTrue(printed.contains("Available Missions"));
        assertTrue(printed.contains("Operation Alpha"));
    }
    @Test
    void userCanAddMissionAndThenSeeItInList() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "4\n" +                  // Missions
                        "3\n" +                  // Add mission
                        "Operation Beta\n" +
                        "2024-06-01\n" +
                        "Beta description.\n" +
                        "1\n" +                  // Facility ID
                        "1\n" +                  // List missions
                        "0\n" +                  // Return without reading
                        "4\n" +                  // Back
                        "7\n"                    // Exit
        );
        String printed = runWithCapturedOutput(scanner);
        assertTrue(printed.contains("Mission added successfully."));
        assertTrue(printed.contains("Operation Beta"));
    }
    @Test
    void invalidMissionsMenuOptionShowsError() {
        Scanner scanner = new Scanner(
                "agent\n" +
                        "Abcde\n" +
                        "4\n" +    // Missions
                        "wrong\n" +
                        "4\n" +    // Back
                        "7\n"      // Exit
        );
        String printed = runWithCapturedOutput(scanner);
        assertTrue(printed.contains("Missions Menu"));
        assertTrue(printed.contains("Invalid option."));
    }
}