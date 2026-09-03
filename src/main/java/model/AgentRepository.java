package model;

import java.sql.SQLException;
import java.util.List;

public interface AgentRepository {
    //reading
    List<Agent> getAllAgents() throws SQLException;
    List<Agent> getAgentsByFacility(int facilityId) throws SQLException;
    Agent getAgentById(int id) throws SQLException;
    //writes
    void addAgent(String name, String dateOfBirth, String dateOfDeath, String notes, int facilityId) throws SQLException;
    void deleteAgent(int id) throws SQLException;
}
