package com.campus.model;

/**
 * AuditEntry — Represents a single audit log record.
 *
 * Unit 4: Stored in a Vector (thread-safe in-memory buffer) and persisted via AuditDAO.
 */
public class AuditEntry {

    private int     logId;
    private Integer userId;    // nullable (e.g., anonymous registration attempts)
    private String  action;
    private String  details;
    private String  timestamp;

    // Constructor for new entries (no DB ID yet)
    public AuditEntry(Integer userId, String action, String details, String timestamp) {
        this.userId    = userId;
        this.action    = action;
        this.details   = details;
        this.timestamp = timestamp;
    }

    // Constructor for entries loaded from DB
    public AuditEntry(int logId, Integer userId, String action, String details, String timestamp) {
        this(userId, action, details, timestamp);
        this.logId = logId;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int     getLogId()    { return logId; }
    public Integer getUserId()   { return userId; }
    public String  getAction()   { return action; }
    public String  getDetails()  { return details; }
    public String  getTimestamp(){ return timestamp; }

    public void setLogId(int id) { this.logId = id; }

    @Override
    public String toString() {
        String uid = (userId != null) ? "User #" + userId : "SYSTEM";
        return String.format("[%s] [%s] %s — %s", timestamp, uid, action, details);
    }
}
