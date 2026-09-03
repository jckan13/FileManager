package controller;

import model.Agent;
import model.DatabaseManager;
import model.Facility;
import model.Mission;
import view.HtmlView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class HtmlViewDatabaseIntegrationTest {

    private DatabaseManager dbManager;
    private HtmlView htmlView;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        dbManager = new DatabaseManager(":memory:");
        dbManager.initializeDatabase();

        // Clear imported test data so this test controls the database.
        dbManager.clearMissions();
        dbManager.clearAgents();
        dbManager.clearFacilities();

        // Reconnect ProgramControl to the in-memory database.
        ProgramControl.setRepositories(dbManager);
        ProgramControl.setCurrentUsername("tester");

        htmlView = new HtmlView();

        // Set up sample data.
        dbManager.addFacility("Alpha Facility", "ALP");
        dbManager.addAgent("Agent Smith", "1980-01-01", "", "Field agent.", 1);
        dbManager.addMission("Operation Alpha", "2024-01-01", "Alpha mission description.", 1);
        dbManager.addBrief("Alpha Brief", "2024-01-02", "Full alpha brief text.", 1);
        dbManager.addAgentToMission(1, 1);
    }

    @Test
    void homePageContainsNavigationLinks() {
        String html = htmlView.homePage();

        assertTrue(html.contains("Top Secret Database"));
        assertTrue(html.contains("/agents"));
        assertTrue(html.contains("/facilities"));
        assertTrue(html.contains("/missions"));
        assertTrue(html.contains("/briefs"));
        assertTrue(html.contains("/audit"));
    }

    @Test
    void agentsPageShowsDataFromDatabase() {
        String agentText = ProgramControl.listAgents();
        String html = htmlView.agentsPage(agentText);

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Agents"));
        assertTrue(html.contains("Agent Smith"));
        assertTrue(html.contains("Back to Home"));
    }

    @Test
    void facilitiesPageShowsDataFromDatabase() {
        String facilityText = ProgramControl.listFacilities();
        String html = htmlView.facilitiesPage(facilityText);

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Facilities"));
        assertTrue(html.contains("Alpha Facility"));
        assertTrue(html.contains("ALP"));
        assertTrue(html.contains("Back to Home"));
    }

    @Test
    void missionsPageShowsDataFromDatabase() {
        String missionText = ProgramControl.listMissions();
        String html = htmlView.missionsPage(missionText);

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Missions"));
        assertTrue(html.contains("Operation Alpha"));
        assertTrue(html.contains("2024-01-01"));
        assertTrue(html.contains("Back to Home"));
    }

    @Test
    void briefsPageShowsDataFromDatabase() {
        String briefText = ProgramControl.listBriefs();
        String html = htmlView.briefsPage(briefText);

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Briefs"));
        assertTrue(html.contains("Alpha Brief"));
        assertTrue(html.contains("2024-01-02"));
        assertTrue(html.contains("Back to Home"));
    }

    @Test
    void briefDetailPageShowsFullBriefTextFromDatabase() {
        String detailText = ProgramControl.displayBrief("1");
        String html = htmlView.wrapPage(
                "Brief Details",
                "<pre>" + detailText + "</pre><p><a href=\"/briefs\">Back to Briefs</a></p>"
        );

        assertTrue(html.contains("Brief Details"));
        assertTrue(html.contains("Alpha Brief"));
        assertTrue(html.contains("2024-01-02"));
        assertTrue(html.contains("Mission ID: 1"));
        assertTrue(html.contains("Full alpha brief text."));
    }

    @Test
    void missionDetailPageShowsFullMissionInfoFromDatabase() {
        String detailText = ProgramControl.displayMission("1");
        String html = htmlView.wrapPage(
                "Mission Details",
                "<pre>" + detailText + "</pre><p><a href=\"/missions\">Back to Missions</a></p>"
        );

        assertTrue(html.contains("Mission Details"));
        assertTrue(html.contains("Operation Alpha"));
        assertTrue(html.contains("2024-01-01"));
        assertTrue(html.contains("Facility ID: 1"));
        assertTrue(html.contains("Alpha mission description."));
    }

    @Test
    void agentDetailPageShowsFullAgentInfoFromDatabase() {
        String detailText = ProgramControl.displayAgent("1");
        String html = htmlView.wrapPage(
                "Agent Details",
                "<pre>" + detailText + "</pre><p><a href=\"/agents\">Back to Agents</a></p>"
        );

        assertTrue(html.contains("Agent Details"));
        assertTrue(html.contains("Agent Smith"));
        assertTrue(html.contains("1980-01-01"));
        assertTrue(html.contains("Field agent."));
        assertTrue(html.contains("Facility ID: 1"));
    }

    @Test
    void facilityDetailPageShowsFullFacilityInfoFromDatabase() {
        String detailText = ProgramControl.displayFacility("1");
        String html = htmlView.wrapPage(
                "Facility Details",
                "<pre>" + detailText + "</pre><p><a href=\"/facilities\">Back to Facilities</a></p>"
        );

        assertTrue(html.contains("Facility Details"));
        assertTrue(html.contains("Alpha Facility"));
        assertTrue(html.contains("ALP"));
    }

    @Test
    void auditPageShowsLoggedReadActionFromDatabase() {
        ProgramControl.displayBrief("1");

        String auditText = ProgramControl.listAuditLogs();
        String html = htmlView.auditPage(auditText);

        assertTrue(html.contains("Audit Log"));
        assertTrue(html.contains("read"));
        assertTrue(html.contains("brief"));
        assertTrue(html.contains("tester"));
    }

    @Test
    void databaseObjectsCanBeRetrievedDirectly() throws SQLException {
        Facility facility = dbManager.getFacilityById(1);
        Agent agent = dbManager.getAgentById(1);
        Mission mission = dbManager.getMissionById(1);

        assertNotNull(facility);
        assertNotNull(agent);
        assertNotNull(mission);

        assertEquals("Alpha Facility", facility.getName());
        assertEquals("Agent Smith", agent.getName());
        assertEquals("Operation Alpha", mission.getTitle());
    }

    @Test
    void linkedBriefListCreatesClickableBriefLinks() {
        String html = htmlView.linkedListPage(
                "Briefs",
                ProgramControl.listBriefs(),
                "/briefs"
        );

        assertTrue(html.contains("<a href=\"/briefs?number=1\">"));
        assertTrue(html.contains("Alpha Brief"));
    }

    @Test
    void linkedMissionListCreatesClickableMissionLinks() {
        String html = htmlView.linkedListPage(
                "Missions",
                ProgramControl.listMissions(),
                "/missions"
        );

        assertTrue(html.contains("<a href=\"/missions?number=1\">"));
        assertTrue(html.contains("Operation Alpha"));
    }

    @Test
    void linkedAgentListCreatesClickableAgentLinks() {
        String html = htmlView.linkedListPage(
                "Agents",
                ProgramControl.listAgents(),
                "/agents"
        );

        assertTrue(html.contains("<a href=\"/agents?number=1\">"));
        assertTrue(html.contains("Agent Smith"));
    }

    @Test
    void linkedFacilityListCreatesClickableFacilityLinks() {
        String html = htmlView.linkedListPage(
                "Facilities",
                ProgramControl.listFacilities(),
                "/facilities"
        );

        assertTrue(html.contains("<a href=\"/facilities?number=1\">"));
        assertTrue(html.contains("Alpha Facility"));
    }
}