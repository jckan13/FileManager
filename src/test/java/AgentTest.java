import model.Agent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {
    //testing constructor and getters
    @Test
    @DisplayName("Agent constructor sets all fields correctly")
    void testConstructor() {
        Agent a = new Agent(1, "Vanguard", "June 14 1985", "2020-01-01","Deceased",1);
        assertEquals(1, a.getId());
        assertEquals("Vanguard", a.getName());
        assertEquals("June 14 1985", a.getDateOfBirth());
        assertEquals("2020-01-01", a.getDateOfDeath());
        assertEquals("Deceased", a.getNotes());
        assertEquals(1, a.getFacilityId());
    }
    @Test
    @DisplayName("getId returns correct id")
    void testGetId() {
        Agent a = new Agent(42, "Ghost", "November 22, 1992", "", "", 1);
        assertEquals(42, a.getId());
    }
    @Test
    @DisplayName("getName returns correct name")
    void testGetName() {
        Agent a = new Agent(1, "Mirage", "March 03, 1988", "", "", 1);
        assertEquals("Mirage", a.getName());
    }
    @Test
    @DisplayName("getDateOfBirth returns correct date of birth")
    void testGetDateOfBirth() {
        Agent a = new Agent(1, "Cinder", "September 12, 1995", "", "", 1);
        assertEquals("September 12, 1995", a.getDateOfBirth());
    }
    @Test
    @DisplayName("getDateOfDeath returns correct date of death")
    void testGetDateOfDeath() {
        Agent a = new Agent(1, "Falcon", "January 30, 1982", "2015-06-01", "", 1);
        assertEquals("2015-06-01", a.getDateOfDeath());
    }
    @Test
    @DisplayName("getDateOfDeath returns empty string when agent is alive")
    void testGetDateOfDeathEmpty() {
        Agent a = new Agent(1, "Vanguard", "June 14, 1985", "", "", 1);
        assertEquals("", a.getDateOfDeath());
    }
    @Test@DisplayName("getNotes returns correctly notes")
    void testGetNotes() {
        Agent a = new Agent(1, "Ghost", "November 22, 1992", "", "Field operative", 1);
        assertEquals("Field operative", a.getNotes());
    }
    @Test
    @DisplayName("getNotes returns empty string when no notes")
    void testGetNotesEmpty() {
        Agent a = new Agent(1, "Ghost", "November 22, 1992", "", "", 1);
        assertEquals("", a.getNotes());
    }
    @Test
    @DisplayName("getFacilityId returns correct facility id")
    void testGetFacilityId() {
        Agent a = new Agent(1, "Vanguard", "June 14, 1985", "", "", 3);
        assertEquals(3, a.getFacilityId());
    }

}