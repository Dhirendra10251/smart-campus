package com.campus;

import com.campus.dao.*;
import com.campus.service.*;
import com.campus.ui.ConsoleUI;
import com.campus.util.DatabaseInitializer;
import com.campus.util.DatabaseManager;
import com.campus.util.FileUtil;
import com.campus.util.Logger;

import java.util.Scanner;

/**
 * Main — Application entry point.
 *
 * Responsibilities:
 * 1. Initialise directories and database.
 * 2. Wire up DAOs, services, and UI (manual dependency injection — no framework).
 * 3. Start the main UI loop.
 * 4. Handle graceful shutdown.
 *
 * Unit 1: basic Java structure, main method, try-catch-finally.
 */
public class Main {

    public static void main(String[] args) {
        // ----------------------------------------------------------------
        // Step 1: Ensure required directories exist
        // ----------------------------------------------------------------
        FileUtil.createDirIfNeeded("data");
        FileUtil.createDirIfNeeded("data/logs");
        FileUtil.createDirIfNeeded("data/reports");
        FileUtil.createDirIfNeeded("data/backups");

        Logger.info("===== Application starting =====");

        // ----------------------------------------------------------------
        // Step 2: Initialize database (schema + seed)
        // ----------------------------------------------------------------
        boolean firstRun = false;
        try {
            firstRun = DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("[FATAL] Database initialization failed: " + e.getMessage());
            Logger.error("Database init failure", e);
            System.exit(1);
        }

        if (firstRun) {
            System.out.println("[INFO] Database initialized with demo data.");
            System.out.println("[INFO] Demo credentials:");
            System.out.println("         Admin  : admin@campus.edu   / admin123");
            System.out.println("         Student: alice@campus.edu   / student123");
            System.out.println("         Student: bob@campus.edu     / student123");
        }

        // ----------------------------------------------------------------
        // Step 3: Wire DAOs
        // ----------------------------------------------------------------
        UserDAO           userDAO    = new UserDAO();
        ResourceDAO       resDAO     = new ResourceDAO();
        BookingDAO        bookDAO    = new BookingDAO();
        ServiceRequestDAO sreqDAO    = new ServiceRequestDAO();
        AuditDAO          auditDAO   = new AuditDAO();

        // ----------------------------------------------------------------
        // Step 4: Wire Services
        // ----------------------------------------------------------------
        AuditService          auditService  = new AuditService(auditDAO);
        AuthService           authService   = new AuthService(userDAO, auditService);
        ResourceService       resService    = new ResourceService(resDAO, auditService);
        BookingService        bookService   = new BookingService(bookDAO, resService, auditService);
        ServiceRequestService sreqService   = new ServiceRequestService(sreqDAO, auditService);
        ReportService         reportService = new ReportService(resService, bookService, sreqService);

        // ----------------------------------------------------------------
        // Step 5: Start UI — try-finally ensures DB closes cleanly
        // ----------------------------------------------------------------
        Scanner scanner = new Scanner(System.in);
        ConsoleUI ui = new ConsoleUI(scanner, authService, resService,
                                     bookService, sreqService, reportService, auditService);
        try {
            ui.start();
        } catch (Exception e) {
            System.err.println("[FATAL] Unexpected error: " + e.getMessage());
            Logger.error("Unexpected fatal error in main loop", e);
        } finally {
            // Unit 3 — finally block: guaranteed cleanup
            scanner.close();
            DatabaseManager.getInstance().closeConnection();
            Logger.info("===== Application shutdown =====");
        }
    }
}
