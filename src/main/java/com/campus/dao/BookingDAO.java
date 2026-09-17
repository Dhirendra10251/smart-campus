package com.campus.dao;

import com.campus.exception.DatabaseOperationException;
import com.campus.model.Booking;
import com.campus.model.enums.BookingStatus;
import com.campus.util.DatabaseManager;
import com.campus.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookingDAO — JDBC data access for the bookings table.
 *
 * Unit 5 JDBC:
 * - PreparedStatement for all queries
 * - Conflict check query with overlap logic
 * - Transaction for concurrent-safe insert (used by BookingService)
 */
public class BookingDAO {

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public int insert(Booking booking) {
        String sql = """
            INSERT INTO bookings
              (user_id, resource_id, booking_date, start_time, end_time, status, created_at)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getResourceId());
            ps.setString(3, booking.getBookingDate());
            ps.setString(4, booking.getStartTime());
            ps.setString(5, booking.getEndTime());
            ps.setString(6, booking.getStatus().name());
            ps.setString(7, booking.getCreatedAt());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    booking.setBookingId(id);
                    return id;
                }
            }
            throw new DatabaseOperationException("Booking insert returned no key.");
        } catch (SQLException e) {
            Logger.error("BookingDAO.insert failed: " + e.getMessage(), e);
            throw new DatabaseOperationException("Failed to insert booking.", e);
        }
    }

    // -------------------------------------------------------------------------
    // CONFLICT CHECK
    //
    // Overlap condition: newStart < existingEnd AND newEnd > existingStart
    // Only confirmed bookings are considered active conflicts.
    // -------------------------------------------------------------------------

    /**
     * Returns true if ANY confirmed booking overlaps the requested slot.
     *
     * @param resourceId   the resource to check
     * @param date         booking date (yyyy-MM-dd)
     * @param startTime    requested start (HH:mm)
     * @param endTime      requested end (HH:mm)
     * @param excludeId    booking ID to exclude (0 = no exclusion)
     */
    public boolean hasConflict(int resourceId, String date,
                               String startTime, String endTime, int excludeId) {
        String sql = """
            SELECT COUNT(*) FROM bookings
            WHERE resource_id = ?
              AND booking_date = ?
              AND status = 'CONFIRMED'
              AND start_time < ?
              AND end_time   > ?
              AND booking_id != ?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setString(2, date);
            ps.setString(3, endTime);   // newEnd > existingStart  →  existingStart < newEnd
            ps.setString(4, startTime); // newStart < existingEnd  →  existingEnd > newStart
            ps.setInt(5, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Conflict check failed.", e);
        }
    }

    // -------------------------------------------------------------------------
    // SELECT
    // -------------------------------------------------------------------------

    public Optional<Booking> findById(int bookingId) {
        String sql = """
            SELECT b.*, u.name AS user_name, r.name AS resource_name
            FROM bookings b
            JOIN users u     ON b.user_id     = u.user_id
            JOIN resources r ON b.resource_id = r.resource_id
            WHERE b.booking_id = ?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find booking by ID.", e);
        }
        return Optional.empty();
    }

    /** Get all bookings for a specific user (Unit 4 — ArrayList). */
    public List<Booking> findByUserId(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT b.*, u.name AS user_name, r.name AS resource_name
            FROM bookings b
            JOIN users u     ON b.user_id     = u.user_id
            JOIN resources r ON b.resource_id = r.resource_id
            WHERE b.user_id = ?
            ORDER BY b.booking_date DESC, b.start_time DESC
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find bookings by user.", e);
        }
        return list;
    }

    /** Get all bookings — admin view. */
    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT b.*, u.name AS user_name, r.name AS resource_name
            FROM bookings b
            JOIN users u     ON b.user_id     = u.user_id
            JOIN resources r ON b.resource_id = r.resource_id
            ORDER BY b.booking_date DESC, b.start_time DESC
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all bookings.", e);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public boolean updateStatus(int bookingId, BookingStatus newStatus) {
        String sql = "UPDATE bookings SET status=? WHERE booking_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update booking status.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Row mapper
    // -------------------------------------------------------------------------

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking(
            rs.getInt("booking_id"),
            rs.getInt("user_id"),
            rs.getInt("resource_id"),
            rs.getString("booking_date"),
            rs.getString("start_time"),
            rs.getString("end_time"),
            BookingStatus.valueOf(rs.getString("status")),
            rs.getString("created_at")
        );
        // Populate denormalized display fields if JOIN was used
        try { b.setUserName(rs.getString("user_name")); }     catch (SQLException ignored) {}
        try { b.setResourceName(rs.getString("resource_name")); } catch (SQLException ignored) {}
        return b;
    }
}
