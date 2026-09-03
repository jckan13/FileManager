package controller;

import model.*;

import java.sql.SQLException;
import java.util.List;

public class ProgramControl {

    private static MissionRepository missionRepository;
    private static AgentRepository agentRepository;
    private static FacilityRepository facilityRepository;
    private static AuditLogRepository auditLogRepository;
    private static String currentUsername = "unknown";
    private static BriefRepository briefRepository;

    public static void setMissionRepository(MissionRepository repo) {
        missionRepository = repo;
    }

    public static void setBriefRepository(BriefRepository repo) {
        briefRepository = repo;
    }

    public static void setAgentRepository(AgentRepository repo) {
        agentRepository = repo;
    }

    public static void setFacilityRepository(FacilityRepository repo) {
        facilityRepository = repo;
    }

    public static void setAuditLogRepository(AuditLogRepository repo) {
        auditLogRepository = repo;
    }

    public static void setCurrentUsername(String username) {
        if (username == null || username.isBlank()) {
            currentUsername = "unknown";
        } else {
            currentUsername = username;
        }
    }

    // Convenience method because DatabaseManager implements all three interfaces
    public static void setRepositories(DatabaseManager dbManager) {
        missionRepository = dbManager;
        briefRepository = dbManager;
        agentRepository = dbManager;
        facilityRepository = dbManager;
        auditLogRepository = dbManager;
    }

    private static void logEvent(String eventType, String contentType, int contentId, String username) {
        if (auditLogRepository == null) {
            return;
        }

        try {
            auditLogRepository.logEvent(eventType, contentType, contentId, username);
        } catch (SQLException e) {
            // Do not crash the program if audit logging fails.
        }
    }

    // -------------------------
// Brief methods
// -------------------------

    public static String listBriefs() {
        if (briefRepository == null) {
            return "Error: Brief repository is not connected.";
        }

        try {
            List<Brief> briefs = briefRepository.getAllBriefs();

            if (briefs == null || briefs.isEmpty()) {
                return "No briefs available.";
            }

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < briefs.size(); i++) {
                Brief brief = briefs.get(i);
                result.append(String.format(
                        "%d. %s (%s)%n",
                        i + 1,
                        brief.getTitle(),
                        brief.getDate()
                ));
            }

            return result.toString().trim();

        } catch (SQLException e) {
            return "Error: Could not retrieve briefs.";
        }
    }

    public static String displayBrief(String number) {
        if (briefRepository == null) {
            return "Error: Brief repository is not connected.";
        }

        try {
            List<Brief> briefs = briefRepository.getAllBriefs();
            int index = Integer.parseInt(number) - 1;

            if (index < 0 || index >= briefs.size()) {
                return "Error: Invalid brief number: " + number;
            }

            Brief brief = briefs.get(index);

            logEvent("read", "brief", brief.getId(), currentUsername);

            return "Title: " + brief.getTitle() + "\n" +
                    "Date: " + brief.getDate() + "\n" +
                    "Mission ID: " + brief.getMissionId() + "\n\n" +
                    brief.getText();

        } catch (SQLException e) {
            return "Error: Could not retrieve brief.";
        } catch (NumberFormatException e) {
            return "Error: Please enter a valid number.";
        }
    }

    public static String addBrief(String title, String date, String text, String missionIdText) {
        if (briefRepository == null) {
            return "Error: Brief repository is not connected.";
        }

        if (title == null || title.isBlank() ||
                date == null || date.isBlank() ||
                text == null || text.isBlank() ||
                missionIdText == null || missionIdText.isBlank()) {
            return "Error: Brief fields cannot be blank.";
        }

        try {
            int missionId = Integer.parseInt(missionIdText);
            briefRepository.addBrief(title, date, text, missionId);
            logEvent("create", "brief", -1, currentUsername);
            return "Brief added successfully.";

        } catch (NumberFormatException e) {
            return "Error: Mission ID must be a number.";
        } catch (SQLException e) {
            return "Error: Could not add brief.";
        }
    }

    // -------------------------
// Mission methods
// -------------------------

    public static String listMissions() {
        if (missionRepository == null) {
            return "Error: Mission repository is not connected.";
        }

        try {
            List<Mission> missions = missionRepository.getAllMissions();

            if (missions == null || missions.isEmpty()) {
                return "No missions available.";
            }

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < missions.size(); i++) {
                Mission mission = missions.get(i);
                result.append(String.format(
                        "%d. %s (%s)%n",
                        i + 1,
                        mission.getTitle(),
                        mission.getDate()
                ));
            }

            return result.toString().trim();

        } catch (SQLException e) {
            return "Error: Could not retrieve missions.";
        }
    }

    public static String displayMission(String number) {
        if (missionRepository == null) {
            return "Error: Mission repository is not connected.";
        }

        try {
            List<Mission> missions = missionRepository.getAllMissions();
            int index = Integer.parseInt(number) - 1;

            if (index < 0 || index >= missions.size()) {
                return "Error: Invalid mission number: " + number;
            }

            Mission mission = missions.get(index);

            logEvent("read", "mission", mission.getId(), currentUsername);

            return "Title: " + mission.getTitle() + "\n" +
                    "Date: " + mission.getDate() + "\n" +
                    "Facility ID: " + mission.getFacilityId() + "\n\n" +
                    "Description: " + mission.getDescription();

        } catch (SQLException e) {
            return "Error: Could not retrieve mission.";
        } catch (NumberFormatException e) {
            return "Error: Please enter a valid number.";
        }
    }

    public static String addMission(String title, String date, String description, String facilityIdText) {
        if (missionRepository == null) {
            return "Error: Mission repository is not connected.";
        }

        if (title == null || title.isBlank() ||
                date == null || date.isBlank() ||
                description == null || description.isBlank() ||
                facilityIdText == null || facilityIdText.isBlank()) {
            return "Error: Mission fields cannot be blank.";
        }

        try {
            int facilityId = Integer.parseInt(facilityIdText);
            missionRepository.addMission(title, date, description, facilityId);
            logEvent("create", "mission", -1, currentUsername);
            return "Mission added successfully.";

        } catch (NumberFormatException e) {
            return "Error: Facility ID must be a number.";
        } catch (SQLException e) {
            return "Error: Could not add mission.";
        }
    }

    // -------------------------
    // Agent methods
    // -------------------------

    public static String listAgents() {
        if (agentRepository == null) {
            return "Error: Agent repository is not connected.";
        }

        try {
            List<Agent> agents = agentRepository.getAllAgents();

            if (agents == null || agents.isEmpty()) {
                return "No agents available.";
            }

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                result.append(String.format(
                        "%d. %s%n",
                        i + 1,
                        agent.getName()
                ));
            }

            return result.toString().trim();

        } catch (SQLException e) {
            return "Error: Could not retrieve agents.";
        }
    }//end of listAgents

    public static String displayAgent(String number) {
        if (agentRepository == null) {
            return "Error: Agent repository is not connected.";
        }

        try {
            List<Agent> agents = agentRepository.getAllAgents();
            int index = Integer.parseInt(number) - 1;

            if (index < 0 || index >= agents.size()) {
                return "Error: Invalid agent number: " + number;
            }

            Agent agent = agents.get(index);

            logEvent("read", "agent", agent.getId(), currentUsername);

            return "Name: " + agent.getName() + "\n" +
                    "Date of Birth: " + agent.getDateOfBirth() + "\n" +
                    "Date of Death: " + agent.getDateOfDeath() + "\n" +
                    "Facility ID: " + agent.getFacilityId() + "\n\n" +
                    "Notes: " + agent.getNotes();

        } catch (SQLException e) {
            return "Error: Could not retrieve agent.";
        } catch (NumberFormatException e) {
            return "Error: Please enter a valid number.";
        }
    }//end of displayAgent

    public static String addAgent(String name, String dateOfBirth, String dateOfDeath,
                                  String notes, String facilityIdText) {
        if (agentRepository == null) {
            return "Error: Agent repository is not connected.";
        }

        if (name == null || name.isBlank() ||
                dateOfBirth == null || dateOfBirth.isBlank() ||
                facilityIdText == null || facilityIdText.isBlank()) {
            return "Error: Agent name, date of birth, and facility ID cannot be blank.";
        }

        if (dateOfDeath == null) {
            dateOfDeath = "";
        }

        if (notes == null) {
            notes = "";
        }

        try {
            int facilityId = Integer.parseInt(facilityIdText);
            agentRepository.addAgent(name, dateOfBirth, dateOfDeath, notes, facilityId);
            logEvent("create", "agent", -1, currentUsername);
            return "Agent added successfully.";

        } catch (NumberFormatException e) {
            return "Error: Facility ID must be a number.";
        } catch (SQLException e) {
            return "Error: Could not add agent.";
        }
    }

    // -------------------------
    // Facility methods
    // -------------------------

    public static String listFacilities() {
        if (facilityRepository == null) {
            return "Error: Facility repository is not connected.";
        }

        try {
            List<Facility> facilities = facilityRepository.getAllFacilities();

            if (facilities == null || facilities.isEmpty()) {
                return "No facilities available.";
            }

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < facilities.size(); i++) {
                Facility facility = facilities.get(i);
                result.append(String.format(
                        "%d. %s (%s)%n",
                        i + 1,
                        facility.getName(),
                        facility.getAbbreviation()
                ));
            }

            return result.toString().trim();

        } catch (SQLException e) {
            return "Error: Could not retrieve facilities.";
        }
    }

    public static String displayFacility(String number) {
        if (facilityRepository == null) {
            return "Error: Facility repository is not connected.";
        }

        try {
            List<Facility> facilities = facilityRepository.getAllFacilities();
            int index = Integer.parseInt(number) - 1;

            if (index < 0 || index >= facilities.size()) {
                return "Error: Invalid facility number: " + number;
            }

            Facility facility = facilities.get(index);

            logEvent("read", "facility", facility.getId(), currentUsername);

            return "Name: " + facility.getName() + "\n" +
                    "Abbreviation: " + facility.getAbbreviation();

        } catch (SQLException e) {
            return "Error: Could not retrieve facility.";
        } catch (NumberFormatException e) {
            return "Error: Please enter a valid number.";
        }
    }

    public static String addFacility(String name, String abbreviation) {
        if (facilityRepository == null) {
            return "Error: Facility repository is not connected.";
        }

        if (name == null || name.isBlank() ||
                abbreviation == null || abbreviation.isBlank()) {
            return "Error: Facility name and abbreviation cannot be blank.";
        }

        try {
            facilityRepository.addFacility(name, abbreviation);
            logEvent("create", "facility", -1, currentUsername);
            return "Facility added successfully.";

        } catch (SQLException e) {
            return "Error: Could not add facility.";
        }
    }

    // -------------------------
    // Audit log
    // -------------------------

    public static String listAuditLogs() {
        if (auditLogRepository == null) {
            return "Error: Audit log repository is not connected.";
        }

        try {
            List<String> logs = auditLogRepository.getAuditLog();

            if (logs == null || logs.isEmpty()) {
                return "No audit logs available.";
            }

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < logs.size(); i++) {
                result.append(i + 1).append(". ").append(logs.get(i)).append("\n");
            }

            return result.toString().trim();

        } catch (SQLException e) {
            return "Error: Could not retrieve audit log.";
        }
    }

}//end of ProgramControl
