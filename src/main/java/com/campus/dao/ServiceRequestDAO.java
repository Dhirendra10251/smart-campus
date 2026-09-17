package com.campus.dao;

import com.campus.exception.DatabaseOperationException;
import com.campus.model.ServiceRequest;
import com.campus.model.enums.Priority;
import com.campus.model.enums.RequestStatus;
import com.campus.util.DatabaseManager;
import com.campus.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ServiceRequestDAO — JDBC data access for the service_requests table.
 *
 * Unit 5 JDBC: PreparedStatement, ResultSet, CRUD.
 */
public class ServiceRequestDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public int insert(ServiceRequest req) {
        String sql = """
            INSERT INTO service_requests
              (user_id, resource_id, description, category, priority, status, created_at, updated_at)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getUserId());
            if (req.getResourceId() != null) ps.setInt(2, req.getResourceId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, req.getDescription());
            ps.setString(4, req.getCategory());
            ps.setString(5, req.getPriority().name());
            ps.setString(6, req.getStatus().name());
            ps.setString(7, req.getCreatedAt());
            ps.setString(8, req.getUpdatedAt());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    req.setRequestId(id);
                    return id;
                }
            }
            throw new DatabaseOperationException("Service request insert returned no key.");
        } catch (SQLException e) {
            Logger.error("ServiceRequestDAO.insert: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to insert service request.", e);
        }
    }

    // -------------------------------------------------------------------------
    // SELECT
    // -------------------------------------------------------------------------

    public Optional<ServiceRequest> findById(int requestId) {
        String sql = buildSelectJoin() + " WHERE sr.request_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find service request by ID.", e);
        }
        return Optional.empty();
    }

    public List<ServiceRequest> findByUserId(int userId) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = buildSelectJoin() + " WHERE sr.user_id = ? ORDER BY sr.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list user service requests.", e);
        }
        return list;
    }

    public List<ServiceRequest> findAll() {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = buildSelectJoin() + " ORDER BY sr.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all service requests.", e);
        }
        return list;
    }

    public List<ServiceRequest> findByStatus(RequestStatus status) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = buildSelectJoin() + " WHERE sr.status = ? ORDER BY sr.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to filter service requests by status.", e);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public boolean updateStatus(int requestId, RequestStatus status, String updatedAt) {
        String sql = "UPDATE service_requests SET status=?, updated_at=? WHERE request_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, updatedAt);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update service request status.", e);
        }
    }

    public boolean updatePriority(int requestId, Priority priority, String updatedAt) {
        String sql = "UPDATE service_requests SET priority=?, updated_at=? WHERE request_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, priority.name());
            ps.setString(2, updatedAt);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update service request priority.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildSelectJoin() {
        return """
            SELECT sr.*, u.name AS user_name,
                   COALESCE(r.name, 'N/A') AS resource_name
            FROM service_requests sr
            JOIN users u ON sr.user_id = u.user_id
            LEFT JOIN resources r ON sr.resource_id = r.resource_id
            """;
    }

    private ServiceRequest mapRow(ResultSet rs) throws SQLException {
        Integer resourceId = rs.getInt("resource_id");
        if (rs.wasNull()) resourceId = null;

        ServiceRequest req = new ServiceRequest(
            rs.getInt("request_id"),
            rs.getInt("user_id"),
            resourceId,
            rs.getString("description"),
            rs.getString("category"),
            Priority.valueOf(rs.getString("priority")),
            RequestStatus.valueOf(rs.getString("status")),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
        try { req.setUserName(rs.getString("user_name")); }     catch (SQLException ignored) {}
        try { req.setResourceName(rs.getString("resource_name")); } catch (SQLException ignored) {}
        return req;
    }
}
