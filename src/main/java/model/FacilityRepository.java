package model;

import java.sql.SQLException;
import java.util.List;

public interface FacilityRepository {
    List<Facility> getAllFacilities() throws SQLException;
    Facility getFacilityById(int id) throws SQLException;
    //writes
    void addFacility(String name, String abbreviation) throws SQLException;
    void deleteFacility(int id) throws SQLException;
}

