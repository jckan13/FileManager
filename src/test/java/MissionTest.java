import model.Mission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissionTest {

    @Test
    @DisplayName("Mission constructor sets all fields correctly")
    void testConstructor() {
        Mission m = new Mission(1, "Op Alpha", "1970-01-01", "Alpha description.", 1);

        assertEquals(1, m.getId());
        assertEquals("Op Alpha", m.getTitle());
        assertEquals("1970-01-01", m.getDate());
        assertEquals("Alpha description.", m.getDescription());
        assertEquals(1, m.getFacilityId());
    }

    @Test
    @DisplayName("getId returns correct id")
    void testGetId() {
        Mission m = new Mission(42, "T", "1970-01-01", "D", 1);

        assertEquals(42, m.getId());
    }

    @Test
    @DisplayName("getTitle returns correct title")
    void testGetTitle() {
        Mission m = new Mission(1, "Operation Sandtrap", "1970-01-01", "D", 1);

        assertEquals("Operation Sandtrap", m.getTitle());
    }

    @Test
    @DisplayName("getDescription returns correct description")
    void testGetDescription() {
        Mission m = new Mission(1, "T", "1970-01-01", "Bug the diplomatic pouch", 1);

        assertEquals("Bug the diplomatic pouch", m.getDescription());
    }

    @Test
    @DisplayName("getDate returns correct date")
    void testGetDate() {
        Mission m = new Mission(1, "T", "1970-09-15", "D", 1);

        assertEquals("1970-09-15", m.getDate());
    }

    @Test
    @DisplayName("getFacilityId returns correct facility id")
    void testGetFacilityId() {
        Mission m = new Mission(1, "T", "1970-01-01", "D", 3);

        assertEquals(3, m.getFacilityId());
    }
}