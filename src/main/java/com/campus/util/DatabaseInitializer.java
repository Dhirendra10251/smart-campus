package com.campus.util;

import com.campus.exception.DatabaseOperationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DatabaseInitializer — Creates schema tables and seeds demo data.
 *
 * Idempotent: uses CREATE TABLE IF NOT EXISTS so safe to call on every startup.
 * Seed data insertion uses INSERT OR IGNORE on unique columns to avoid duplicates.
 */
public class DatabaseInitializer {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Entry point called from Main on startup.
     * Returns true if this was the first initialization (schema just created).
     */
    public static boolean initialize() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            createTables(conn);
            boolean seeded = seedDemoData(conn);
            return seeded;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Schema creation
    // -------------------------------------------------------------------------

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // USERS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    name         TEXT    NOT NULL,
                    email        TEXT    UNIQUE NOT NULL,
                    password_hash TEXT   NOT NULL,
                    role         TEXT    NOT NULL DEFAULT 'STUDENT',
                    created_at   TEXT    NOT NULL
                )
                """);

            // RESOURCES
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS resources (
                    resource_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                    name         TEXT    NOT NULL,
                    type         TEXT    NOT NULL,
                    location     TEXT    NOT NULL,
                    capacity     INTEGER DEFAULT 0,
                    status       TEXT    NOT NULL DEFAULT 'AVAILABLE',
                    created_at   TEXT    NOT NULL
                )
                """);

            // BOOKINGS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    booking_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id      INTEGER NOT NULL,
                    resource_id  INTEGER NOT NULL,
                    booking_date TEXT    NOT NULL,
                    start_time   TEXT    NOT NULL,
                    end_time     TEXT    NOT NULL,
                    status       TEXT    NOT NULL DEFAULT 'CONFIRMED',
                    created_at   TEXT    NOT NULL,
                    FOREIGN KEY (user_id)     REFERENCES users(user_id),
                    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
                )
                """);

            // SERVICE_REQUESTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS service_requests (
                    request_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id      INTEGER NOT NULL,
                    resource_id  INTEGER,
                    description  TEXT    NOT NULL,
                    category     TEXT    NOT NULL DEFAULT 'GENERAL',
                    priority     TEXT    NOT NULL DEFAULT 'MEDIUM',
                    status       TEXT    NOT NULL DEFAULT 'OPEN',
                    created_at   TEXT    NOT NULL,
                    updated_at   TEXT    NOT NULL,
                    FOREIGN KEY (user_id)     REFERENCES users(user_id),
                    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
                )
                """);

            // AUDIT_LOGS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    log_id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id   INTEGER,
                    action    TEXT NOT NULL,
                    details   TEXT,
                    timestamp TEXT NOT NULL
                )
                """);
        }
    }

    // -------------------------------------------------------------------------
    // Demo seed data
    // -------------------------------------------------------------------------

    private static boolean seedDemoData(Connection conn) throws SQLException {
        // Check if demo admin already exists — skip if so
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE email = ?")) {
            ps.setString(1, "admin@campus.edu");
            var rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // already seeded
            }
        }

        String now = LocalDateTime.now().format(DT_FMT);

        // Demo passwords (SHA-256 + salt handled by PasswordUtil)
        // admin123  →  hashed
        String adminHash   = PasswordUtil.hashPassword("admin123");
        // student123 → hashed
        String studentHash = PasswordUtil.hashPassword("student123");

        // --- Users ---
        insertUser(conn, "Admin User",       "admin@campus.edu",   adminHash,   "ADMIN",   now);
        insertUser(conn, "Alice Johnson",    "alice@campus.edu",   studentHash, "STUDENT", now);
        insertUser(conn, "Bob Smith",        "bob@campus.edu",     studentHash, "STUDENT", now);

        // --- Resources ---
        insertResource(conn, "Computer Lab A",    "LAB",             "Block A, Floor 1", 40, "AVAILABLE",         now);
        insertResource(conn, "Computer Lab B",    "LAB",             "Block A, Floor 2", 40, "AVAILABLE",         now);
        insertResource(conn, "Seminar Hall 101",  "SEMINAR_HALL",    "Block B, Floor 1", 80, "AVAILABLE",         now);
        insertResource(conn, "Seminar Hall 202",  "SEMINAR_HALL",    "Block B, Floor 2", 60, "AVAILABLE",         now);
        insertResource(conn, "Classroom 301",     "CLASSROOM",       "Block C, Floor 3", 60, "AVAILABLE",         now);
        insertResource(conn, "Classroom 302",     "CLASSROOM",       "Block C, Floor 3", 60, "AVAILABLE",         now);
        insertResource(conn, "Basketball Court",  "SPORTS_FACILITY", "Sports Complex",   30, "AVAILABLE",         now);
        insertResource(conn, "Badminton Court",   "SPORTS_FACILITY", "Sports Complex",   10, "AVAILABLE",         now);
        insertResource(conn, "Projector Set 1",   "EQUIPMENT",       "AV Store Room",     1, "AVAILABLE",         now);
        insertResource(conn, "Projector Set 2",   "EQUIPMENT",       "AV Store Room",     1, "UNDER_MAINTENANCE", now);

        // --- Sample bookings (user_id=2 = Alice, user_id=3 = Bob) ---
        insertBooking(conn, 2, 1, "2026-09-10", "09:00", "11:00", "CONFIRMED", now);
        insertBooking(conn, 2, 3, "2026-09-11", "14:00", "16:00", "CONFIRMED", now);
        insertBooking(conn, 3, 2, "2026-09-10", "10:00", "12:00", "CONFIRMED", now);
        insertBooking(conn, 3, 7, "2026-09-12", "07:00", "09:00", "CANCELLED", now);

        // --- Sample service requests ---
        insertServiceRequest(conn, 2, 1, "AC not working in Computer Lab A", "MAINTENANCE", "HIGH",   "OPEN",        now);
        insertServiceRequest(conn, 3, 7, "Basketball court needs new nets",   "REPAIR",      "MEDIUM", "IN_PROGRESS", now);
        insertServiceRequest(conn, 2, null, "Need extra chairs for seminar",  "GENERAL",     "LOW",    "RESOLVED",    now);

        return true; // seeded for the first time
    }

    // -------------------------------------------------------------------------
    // Helper insert methods
    // -------------------------------------------------------------------------

    private static void insertUser(Connection conn, String name, String email,
                                    String hash, String role, String now) throws SQLException {
        String sql = "INSERT OR IGNORE INTO users (name, email, password_hash, role, created_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, role);
            ps.setString(5, now);
            ps.executeUpdate();
        }
    }

    private static void insertResource(Connection conn, String name, String type,
                                        String location, int capacity,
                                        String status, String now) throws SQLException {
        String sql = "INSERT INTO resources (name, type, location, capacity, status, created_at) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, location);
            ps.setInt(4, capacity);
            ps.setString(5, status);
            ps.setString(6, now);
            ps.executeUpdate();
        }
    }

    private static void insertBooking(Connection conn, int userId, int resourceId,
                                       String date, String start, String end,
                                       String status, String now) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, resource_id, booking_date, start_time, end_time, status, created_at) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            ps.setString(3, date);
            ps.setString(4, start);
            ps.setString(5, end);
            ps.setString(6, status);
            ps.setString(7, now);
            ps.executeUpdate();
        }
    }

    private static void insertServiceRequest(Connection conn, int userId, Integer resourceId,
                                              String desc, String category,
                                              String priority, String status,
                                              String now) throws SQLException {
        String sql = "INSERT INTO service_requests (user_id, resource_id, description, category, priority, status, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (resourceId != null) ps.setInt(2, resourceId);
            else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, desc);
            ps.setString(4, category);
            ps.setString(5, priority);
            ps.setString(6, status);
            ps.setString(7, now);
            ps.setString(8, now);
            ps.executeUpdate();
        }
    }
}
