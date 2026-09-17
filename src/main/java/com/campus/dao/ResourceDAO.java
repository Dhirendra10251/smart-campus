package com.campus.dao;

import com.campus.exception.DatabaseOperationException;
import com.campus.model.Resource;
import com.campus.model.enums.ResourceStatus;
import com.campus.model.enums.ResourceType;
import com.campus.util.DatabaseManager;
import com.campus.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ResourceDAO — JDBC data access for the resources table.
 *
 * Unit 5 JDBC: PreparedStatement, ResultSet, INSERT/SELECT/UPDATE/DELETE.
 */
public class ResourceDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public int insert(Resource resource) {
        String sql = "INSERT INTO resources (name, type, location, capacity, status, created_at) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getName());
            ps.setString(2, resource.getType().name());
            ps.setString(3, resource.getLocation());
            ps.setInt(4, resource.getCapacity());
            ps.setString(5, resource.getStatus().name());
            ps.setString(6, resource.getCreatedAt());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    resource.setResourceId(id);
                    return id;
                }
            }
            throw new DatabaseOperationException("Resource insert returned no key.");
        } catch (SQLException e) {
            Logger.error("ResourceDAO.insert: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to insert resource.", e);
        }
    }

    // -------------------------------------------------------------------------
    // SELECT
    // -------------------------------------------------------------------------

    public Optional<Resource> findById(int resourceId) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find resource by ID.", e);
        }
        return Optional.empty();
    }

    /** Returns all resources (ArrayList — Unit 4). */
    public List<Resource> findAll() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY resource_id";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list resources.", e);
        }
        return list;
    }

    /** Filter by type. */
    public List<Resource> findByType(ResourceType type) {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE type = ? ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to filter resources by type.", e);
        }
        return list;
    }

    /** Case-insensitive name search. */
    public List<Resource> searchByName(String keyword) {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE LOWER(name) LIKE ? OR LOWER(location) LIKE ?";
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search resources.", e);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public boolean update(Resource resource) {
        String sql = "UPDATE resources SET name=?, type=?, location=?, capacity=?, status=? WHERE resource_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, resource.getName());
            ps.setString(2, resource.getType().name());
            ps.setString(3, resource.getLocation());
            ps.setInt(4, resource.getCapacity());
            ps.setString(5, resource.getStatus().name());
            ps.setInt(6, resource.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update resource.", e);
        }
    }

    public boolean updateStatus(int resourceId, ResourceStatus newStatus) {
        String sql = "UPDATE resources SET status=? WHERE resource_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update resource status.", e);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public boolean delete(int resourceId) {
        String sql = "DELETE FROM resources WHERE resource_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete resource.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Row mapper
    // -------------------------------------------------------------------------

    private Resource mapRow(ResultSet rs) throws SQLException {
        return new Resource(
            rs.getInt("resource_id"),
            rs.getString("name"),
            ResourceType.valueOf(rs.getString("type")),
            rs.getString("location"),
            rs.getInt("capacity"),
            ResourceStatus.valueOf(rs.getString("status")),
            rs.getString("created_at")
        );
    }
}
