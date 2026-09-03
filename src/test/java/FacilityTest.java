import model.Facility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacilityTest {
    //constructor and getter test
    @Test
    @DisplayName("Facility constructor sets all fields correctly")
    void testConstructor() {
        Facility f = new Facility(1, "Blackwood Annex", "BA");
        assertEquals(1, f.getId());
        assertEquals("Blackwood Annex", f.getName());
        assertEquals("BA", f.getAbbreviation());
    }
    @Test
    @DisplayName("getId returns correct id")
    void testGetId() {
        Facility f = new Facility(42, "Blackwood Annex", "BA");
        assertEquals(42, f.getId());
    }
    @Test
    @DisplayName("getName returns correct name")
    void testGetName() {
        Facility f = new Facility(1, "Rose Garden", "RG");
        assertEquals("Rose Garden", f.getName());
    }
    @Test
    @DisplayName("getAbbreviation returns correct abbreviation")
    void testGetAbbreviation() {
        Facility f = new Facility(1, "Echo Point", "EP");
        assertEquals("EP", f.getAbbreviation());
    }
    //testing toString
    @Test
    @DisplayName("toString returns correct setup")
    void testToString() {
        Facility f = new Facility(1, "Blackwood Annex", "BA");
        assertEquals("1: Blackwood Annex (BA)", f.toString());
    }
    @Test
    @DisplayName("toString uses correct id in output")
    void testToStringWithDiffID(){
        Facility f = new Facility(5, "Glass House", "GA");
        assertEquals("5: Glass House (GA)", f.toString());
    }

}