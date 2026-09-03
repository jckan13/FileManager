import org.junit.jupiter.api.Test;
import view.HtmlView;

import static org.junit.jupiter.api.Assertions.*;

class HtmlViewTest {
    @Test
    void homePageHasLinks() {
        HtmlView view = new HtmlView();
        String html = view.homePage();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Agents"));
        assertTrue(html.contains("/agents"));
        assertTrue(html.contains("Facilities"));
        assertTrue(html.contains("Missions"));
    }
    @Test
    void agentsPageDisplaysAgentText() {
        HtmlView view = new HtmlView();
        String html = view.agentsPage("Ghost\nMirage");
        assertTrue(html.contains("<h1>Agents</h1>"));
        assertTrue(html.contains("<pre>"));
        assertTrue(html.contains("Ghost"));
        assertTrue(html.contains("Mirage"));
        assertTrue(html.contains("Back to Home"));
        assertTrue(html.contains("</html>"));
    }
    @Test
    void facilitiesPageDisplaysFacilityText() {
        HtmlView view = new HtmlView();
        String html = view.facilitiesPage("Headquarters\nSafe House");
        assertTrue(html.contains("<h1>Facilities</h1>"));
        assertTrue(html.contains("Headquarters"));
        assertTrue(html.contains("Safe House"));
        assertTrue(html.contains("Back to Home"));
    }
    @Test
    void missionsPageDisplaysMissionText() {
        HtmlView view = new HtmlView();
        String html = view.missionsPage("Vanguard\nGhost");
        assertTrue(html.contains("<h1>Missions</h1>"));
        assertTrue(html.contains("Vanguard"));
        assertTrue(html.contains("Ghost"));
        assertTrue(html.contains("Back to Home"));
    }
    @Test
    void briefsPageDisplaysBriefText() {
        HtmlView view = new HtmlView();
        String html = view.briefsPage("Mission briefing details");
        assertTrue(html.contains("<h1>Briefs</h1>"));
        assertTrue(html.contains("Mission briefing details"));
        assertTrue(html.contains("Back to Home"));
    }
    @Test
    void auditPageDisplaysAuditText() {
        HtmlView view = new HtmlView();
        String html = view.auditPage("User logged in");
        assertTrue(html.contains("<h1>Audit Log</h1>"));
        assertTrue(html.contains("User logged in"));
        assertTrue(html.contains("Back to Home"));
    }
    @Test
    void wrapPageCreatesValidHtml() {
        HtmlView view = new HtmlView();
        String html = view.wrapPage("Test", "Hello");
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<h1>Test</h1>"));
        assertTrue(html.contains("Hello"));
        assertTrue(html.contains("</html>"));
    }
}