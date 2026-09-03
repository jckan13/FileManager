package model;

import java.sql.SQLException;
import java.util.List;

public interface AuditLogRepository {
    void logEvent(String eventType, String contentType, int contentId, String username) throws SQLException;

    List<String> getAuditLog() throws SQLException;
}