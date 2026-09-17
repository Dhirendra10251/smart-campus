package com.campus.service;

import com.campus.dao.AuditDAO;
import com.campus.model.AuditEntry;
import com.campus.util.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

/**
 * AuditService — Records audit entries to both the DB and a thread-safe Vector buffer.
 *
 * Unit 4 — Vector usage:
 * Vector<AuditEntry> is used as the in-memory live event buffer.
 * Vector is thread-safe by design (all methods are synchronized), which is
 * appropriate here because the booking concurrency demonstration can produce
 * audit events from multiple threads simultaneously.
 * An ArrayList would require external synchronization; Vector provides it implicitly.
 */
public class AuditService {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditDAO auditDAO;

    /**
     * Thread-safe in-memory buffer of the session's audit events.
     * Justified by multi-threaded booking demo — multiple threads write here concurrently.
     * Unit 4: Vector (thread-safe collection).
     */
    private final Vector<AuditEntry> liveBuffer = new Vector<>();

    public AuditService(AuditDAO auditDAO) {
        this.auditDAO = auditDAO;
    }

    // -------------------------------------------------------------------------
    // Log an event
    // -------------------------------------------------------------------------

    /**
     * Records an audit event: persists to DB, adds to live Vector buffer, and logs to file.
     */
    public void log(Integer userId, String action, String details) {
        String timestamp = LocalDateTime.now().format(DT_FMT);
        AuditEntry entry = new AuditEntry(userId, action, details, timestamp);

        // Persist to database
        auditDAO.insert(entry);

        // Add to thread-safe Vector buffer (survives for this session's admin view)
        liveBuffer.add(entry);

        // Also write to application log file
        Logger.info("AUDIT | " + action + " | " + details);
    }

    // -------------------------------------------------------------------------
    // Retrieve
    // -------------------------------------------------------------------------

    public List<AuditEntry> getAllLogs() {
        return auditDAO.findAll();
    }

    public List<AuditEntry> getLogsForUser(int userId) {
        return auditDAO.findByUserId(userId);
    }

    /**
     * Returns the current session's live buffer (from Vector).
     * Demonstrates Unit 4: using Vector's thread-safe collection methods.
     */
    public List<AuditEntry> getLiveSessionLogs() {
        return new java.util.ArrayList<>(liveBuffer);
    }
}
