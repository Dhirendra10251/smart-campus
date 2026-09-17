package com.campus.dao;

import com.campus.exception.DatabaseOperationException;
import com.campus.model.Admin;
import com.campus.model.Student;
import com.campus.model.User;
import com.campus.model.enums.Role;
import com.campus.util.DatabaseManager;
import com.campus.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO — JDBC data access for the users table.
 *
 * Unit 5 JDBC demonstrations:
 * - PreparedStatement (all user-controlled values)
 * - ResultSet iteration
 * - INSERT, SELECT, UPDATE, DELETE
 * - try-with-resources for automatic resource closure
 */
public class UserDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    /**
     * Inserts a new user and returns the generated user_id.
     */
    public int insert(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getCreatedAt());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setUserId(id);
                    return id;
                }
            }
            throw new DatabaseOperationException("User insert did not return a generated key.");
        } catch (SQLException e) {
            Logger.error("UserDAO.insert failed: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to insert user: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // SELECT
    // -------------------------------------------------------------------------

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error("UserDAO.findByEmail failed: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to find user by email.", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error("UserDAO.findById failed: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to find user by ID.", e);
        }
        return Optional.empty();
    }

    /**
     * Returns all users as an ArrayList (Unit 4 — ArrayList usage).
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error("UserDAO.findAll failed: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve all users.", e);
        }
        return users;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public void updateRole(int userId, Role newRole) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newRole.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update user role.", e);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete user.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Row mapper — converts ResultSet row to User subtype
    // -------------------------------------------------------------------------

    private User mapRow(ResultSet rs) throws SQLException {
        int    id    = rs.getInt("user_id");
        String name  = rs.getString("name");
        String email = rs.getString("email");
        String hash  = rs.getString("password_hash");
        String role  = rs.getString("role");
        String ts    = rs.getString("created_at");

        // Polymorphic object creation based on role
        if (Role.ADMIN.name().equals(role)) {
            return new Admin(id, name, email, hash, ts);
        } else {
            return new Student(id, name, email, hash, ts);
        }
    }
}
