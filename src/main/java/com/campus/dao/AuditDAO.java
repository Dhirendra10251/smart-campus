package com.campus.dao;

import com.campus.exception.DatabaseOperationException;
import com.campus.model.AuditEntry;
import com.campus.util.DatabaseManager;
import com.campus.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditDAO — JDBC data access for the audit_logs table.
 *
 * Unit 5 JDBC: PreparedStatement, INSERT, SELECT.
 */
public class AuditDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public int insert(AuditEntry entry) {
        String sql = "INSERT INTO audit_logs (user_id, action, details, timestamp) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entry.getUserId() != null) ps.setInt(1, entry.getUserId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, entry.getAction());
            ps.setString(3, entry.getDetails());
            ps.setString(4, entry.getTimestamp());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    entry.setLogId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            // Audit failures must not crash the application
            Logger.error("AuditDAO.insert failed (non-fatal): " + e.getMessage(), e);
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // SELECT
    // -------------------------------------------------------------------------

    /** Returns all audit log entries ordered newest-first. */
    public List<AuditEntry> findAll() {
        List<AuditEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY log_id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve audit logs.", e);
        }
        return list;
    }

    /** Audit entries for a specific user. */
    public List<AuditEntry> findByUserId(int userId) {
        List<AuditEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY log_id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve audit logs by user.", e);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Row mapper
    // -------------------------------------------------------------------------

    private AuditEntry mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("log_id");
        int uid = rs.getInt("user_id");
        Integer userId = rs.wasNull() ? null : uid;
        return new AuditEntry(id, userId,
            rs.getString("action"),
            rs.getString("details"),
            rs.getString("timestamp"));
    }
}
