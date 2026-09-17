package com.campus.ui;

import com.campus.exception.BookingConflictException;
import com.campus.exception.InvalidInputException;
import com.campus.exception.InvalidLoginException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.model.Resource;
import com.campus.model.User;
import com.campus.model.enums.Role;
import com.campus.service.*;
import com.campus.util.Logger;

import java.util.List;
import java.util.Scanner;

/**
 * ConsoleUI — Main application loop and top-level menu.
 *
 * Unit 1 demonstration:
 * - switch-case for menu navigation
 * - while loop for persistent menu
 * - Scanner for I/O
 * - String handling (trim, toLowerCase)
 * - break/continue
 *
 * Unit 2:
 * - Polymorphism: calls user.getDashboardTitle() regardless of Student/Admin subtype
 */
public class ConsoleUI {

    private static final String BANNER = """
            
            +==============================================================+
            |       SMART CAMPUS RESOURCE & SERVICE MANAGEMENT SYSTEM      |
            |                    VITyarthi Edition v1.0                    |
            +==============================================================+
            """;

    private final Scanner               scanner;
    private final AuthService           authService;
    private final ResourceService       resourceService;
    private final BookingService        bookingService;
    private final ServiceRequestService requestService;
    private final ReportService         reportService;
    private final AuditService          auditService;

    private StudentMenu studentMenu;
    private AdminMenu   adminMenu;

    public ConsoleUI(Scanner scanner,
                     AuthService authService,
                     ResourceService resourceService,
                     BookingService bookingService,
                     ServiceRequestService requestService,
                     ReportService reportService,
                     AuditService auditService) {
        this.scanner        = scanner;
        this.authService    = authService;
        this.resourceService = resourceService;
        this.bookingService = bookingService;
        this.requestService = requestService;
        this.reportService  = reportService;
        this.auditService   = auditService;

        this.studentMenu = new StudentMenu(scanner, authService, resourceService,
                                           bookingService, requestService, reportService);
        this.adminMenu   = new AdminMenu(scanner, authService, resourceService,
                                         bookingService, requestService, reportService, auditService);
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------

    public void start() {
        System.out.println(BANNER);

        boolean running = true;
        while (running) {
            // If user is logged in, show dashboard
            if (authService.isLoggedIn()) {
                User user = authService.getCurrentUser();
                // Polymorphic call — Student or Admin behaves differently
                if (user.getRole() == Role.ADMIN) {
                    adminMenu.show();
                } else {
                    studentMenu.show();
                }
                // After logout from sub-menu, loop back to main menu
                continue;
            }

            // Not logged in — show main menu
            printMainMenu();
            String choice = scanner.nextLine().trim();

            // Unit 1 — switch-case
            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "3" -> browsePublicResources();
                case "4" -> {
                    System.out.println("\nThank you for using Smart Campus. Goodbye!");
                    Logger.info("Application exited normally.");
                    running = false;
                }
                default -> System.out.println("[ERROR] Invalid choice. Please enter 1-4.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Menu helpers
    // -------------------------------------------------------------------------

    private void printMainMenu() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("  MAIN MENU");
        System.out.println("-".repeat(50));
        System.out.println("  1. Login");
        System.out.println("  2. Register");
        System.out.println("  3. View Public Resources");
        System.out.println("  4. Exit");
        System.out.println("-".repeat(50));
        System.out.print("  Enter choice: ");
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    private void handleLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("  Email   : ");
        String email = scanner.nextLine().trim();
        System.out.print("  Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.login(email, password);
            System.out.println("\n[SUCCESS] Welcome back, " + user.getName() + "! (" + user.getRole() + ")");
        } catch (InvalidLoginException | InvalidInputException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Login failed. Please try again.");
            Logger.error("Login error: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    private void handleRegister() {
        System.out.println("\n--- REGISTER NEW STUDENT ACCOUNT ---");
        System.out.print("  Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("  Email    : ");
        String email = scanner.nextLine().trim();
        System.out.print("  Password : ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.register(name, email, password);
            System.out.println("[SUCCESS] Account created! Welcome, " + user.getName() +
                               ". You can now login.");
        } catch (InvalidInputException | InvalidLoginException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Registration failed. Please try again.");
            Logger.error("Registration error: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Public resource browser (no login required)
    // -------------------------------------------------------------------------

    private void browsePublicResources() {
        try {
            List<Resource> resources = resourceService.getAllResources();
            if (resources.isEmpty()) {
                System.out.println("[INFO] No resources currently listed.");
                return;
            }
            System.out.println("\n--- AVAILABLE CAMPUS RESOURCES ---");
            // Unit 1 — for loop
            for (Resource r : resources) {
                System.out.println("  " + r.toReportLine());
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Could not load resources.");
            Logger.error("browsePublicResources: " + e.getMessage(), e);
        }
    }
}
