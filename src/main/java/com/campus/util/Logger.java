package com.campus.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger — Thread-safe file logger that appends to data/logs/application.log.
 *
 * Separate log levels: INFO, WARN, ERROR.
 * Stack traces are written to the log but never shown to CLI users.
 */
public class Logger {

    private static final String LOG_DIR  = "data/logs";
    private static final String LOG_FILE = "data/logs/application.log";
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Lock object for thread-safe file writes
    private static final Object LOCK = new Object();

    static {
        FileUtil.createDirIfNeeded(LOG_DIR);
    }

    public static void info(String message) {
        write("INFO ", message);
    }

    public static void warn(String message) {
        write("WARN ", message);
    }

    public static void error(String message) {
        write("ERROR", message);
    }

    public static void error(String message, Throwable t) {
        write("ERROR", message + " | " + t.getClass().getSimpleName() + ": " + t.getMessage());
    }

    private static void write(String level, String message) {
        String timestamp = LocalDateTime.now().format(FMT);
        String line = String.format("[%s] [%s] %s%n", timestamp, level, message);

        // Synchronized block — prevents garbled output when multiple threads log
        synchronized (LOCK) {
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(LOG_FILE, true))) { // true = append mode
                bw.write(line);
            } catch (IOException e) {
                // Last-resort: print to stderr without crashing the app
                System.err.println("[LOGGER FAILURE] " + e.getMessage());
            }
        }
    }
}
