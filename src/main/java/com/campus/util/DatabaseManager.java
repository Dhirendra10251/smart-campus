package com.campus.util;

import com.campus.exception.DatabaseOperationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

/**
 * DatabaseManager — Singleton that provides a single shared SQLite connection.
 *
 * Design decision: SQLite is single-writer by default; a single shared connection
 * avoids WAL-mode conflicts while remaining adequate for a CLI application.
 * For a multi-user server application, a connection pool would be used instead.
 */
public class DatabaseManager {

    private static final String DB_DIR  = "data";
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:data/campus.db";
    private static String dbUrl = DEFAULT_DB_URL;

    // Singleton instance
    private static DatabaseManager instance;

    // Shared connection
    private Connection connection;

    // Private constructor — Singleton pattern
    private DatabaseManager() {
        initConnection();
    }

    /**
     * Sets a custom database URL (e.g. "jdbc:sqlite::memory:" for unit tests)
     * and resets the singleton instance.
     */
    public static synchronized void setDatabaseUrl(String url) {
        if (instance != null) {
            instance.closeConnection();
            instance = null;
        }
        dbUrl = url;
    }

    /**
     * Resets the database URL to the default data/campus.db file.
     */
    public static synchronized void resetToDefaultUrl() {
        setDatabaseUrl(DEFAULT_DB_URL);
    }

    /**
     * Returns the singleton DatabaseManager instance.
     * Thread-safe via synchronized keyword (acceptable for CLI app).
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Creates the data directory if absent, then opens the SQLite connection.
     * SQLite creates the .db file automatically if it does not exist.
     */
    private void initConnection() {
        try {
            // Ensure data/ directory exists
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Load SQLite JDBC driver explicitly (required for some environments)
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(dbUrl);
            // Enable foreign key enforcement for SQLite
            connection.createStatement().execute("PRAGMA foreign_keys = ON");

        } catch (ClassNotFoundException e) {
            throw new DatabaseOperationException(
                "SQLite JDBC driver not found. Ensure sqlite-jdbc is on the classpath.", e);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                "Failed to open database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the active connection, reconnecting if it has been closed.
     */
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initConnection();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to validate connection: " + e.getMessage(), e);
        }
        return connection;
    }

    /**
     * Closes the connection. Called on application shutdown.
     */
    public synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            Logger.error("Error closing database connection: " + e.getMessage());
        }
    }
}
