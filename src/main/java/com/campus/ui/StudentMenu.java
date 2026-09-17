package com.campus.ui;

import com.campus.exception.*;
import com.campus.model.Booking;
import com.campus.model.Resource;
import com.campus.model.ServiceRequest;
import com.campus.model.User;
import com.campus.model.enums.BookingStatus;
import com.campus.model.enums.ResourceType;
import com.campus.service.*;
import com.campus.util.Logger;

import java.util.List;
import java.util.Scanner;

/**
 * StudentMenu — All student-facing menu options.
 *
 * Unit 1: while loops, switch-case, for/for-each loops, break/continue.
 * Unit 2: calls service layer; uses polymorphic User reference.
 * Unit 3: try-catch on every action — user never sees stack traces.
 */
public class StudentMenu {

    private final Scanner               scanner;
    private final AuthService           authService;
    private final ResourceService       resourceService;
    private final BookingService        bookingService;
    private final ServiceRequestService requestService;
    private final ReportService         reportService;

    // Valid categories for service requests (array demonstration — Unit 1)
    private static final String[] REQUEST_CATEGORIES =
        {"MAINTENANCE", "REPAIR", "CLEANING", "IT_SUPPORT", "GENERAL"};

    public StudentMenu(Scanner scanner,
                       AuthService authService,
                       ResourceService resourceService,
                       BookingService bookingService,
                       ServiceRequestService requestService,
                       ReportService reportService) {
        this.scanner         = scanner;
        this.authService     = authService;
        this.resourceService = resourceService;
        this.bookingService  = bookingService;
        this.requestService  = requestService;
        this.reportService   = reportService;
    }

    // -------------------------------------------------------------------------
    // Dashboard loop
    // -------------------------------------------------------------------------

    public void show() {
        User user = authService.getCurrentUser();
        boolean inMenu = true;

        // Unit 1 — while loop
        while (inMenu && authService.isLoggedIn()) {
            printDashboard(user);
            String choice = scanner.nextLine().trim();

            // Unit 1 — switch-case
            switch (choice) {
                case "1"  -> viewProfile(user);
                case "2"  -> browseResources();
                case "3"  -> searchResources();
                case "4"  -> bookResource(user);
                case "5"  -> viewMyBookings(user);
                case "6"  -> cancelBooking(user);
                case "7"  -> raiseServiceRequest(user);
                case "8"  -> viewMyRequests(user);
                case "9"  -> generateMyReport(user);
                case "10" -> {
                    authService.logout();
                    System.out.println("[INFO] Logged out successfully.");
                    inMenu = false;
                }
                default -> System.out.println("[ERROR] Invalid choice. Enter 1-10.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Print dashboard
    // -------------------------------------------------------------------------

    private void printDashboard(User user) {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("  " + user.getDashboardTitle());
        System.out.println("=".repeat(55));
        System.out.println("   1. My Profile");
        System.out.println("   2. Browse Resources");
        System.out.println("   3. Search Resources");
        System.out.println("   4. Book Resource");
        System.out.println("   5. My Bookings");
        System.out.println("   6. Cancel Booking");
        System.out.println("   7. Raise Service Request");
        System.out.println("   8. View My Service Requests");
        System.out.println("   9. Generate My Report");
        System.out.println("  10. Logout");
        System.out.println("=".repeat(55));
        System.out.print("  Enter choice: ");
    }

    // -------------------------------------------------------------------------
    // 1. View Profile
    // -------------------------------------------------------------------------

    private void viewProfile(User user) {
        System.out.println("\n--- MY PROFILE ---");
        System.out.println("  " + user.getProfileSummary());
    }

    // -------------------------------------------------------------------------
    // 2. Browse Resources
    // -------------------------------------------------------------------------

    private void browseResources() {
        try {
            System.out.println("\n--- CAMPUS RESOURCES ---");
            System.out.println("  Filter by type? (Enter type or ENTER to show all)");

            // Unit 1 — for-each loop showing enum values
            System.out.print("  Types: ");
            ResourceType[] types = ResourceType.values();
            for (int i = 0; i < types.length; i++) {
                System.out.print(types[i].name());
                if (i < types.length - 1) System.out.print(", ");
            }
            System.out.println();
            System.out.print("  Filter: ");
            String filter = scanner.nextLine().trim();

            List<Resource> resources;
            if (filter.isEmpty()) {
                resources = resourceService.getAllResources();
            } else {
                resources = resourceService.getResourcesByType(filter);
            }

            if (resources.isEmpty()) {
                System.out.println("  No resources found.");
                return;
            }
            // Unit 1 — for-each loop
            for (Resource r : resources) {
                System.out.println("  " + r.toReportLine());
            }
        } catch (InvalidInputException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Could not load resources.");
            Logger.error("browseResources: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 3. Search Resources
    // -------------------------------------------------------------------------

    private void searchResources() {
        System.out.println("\n--- SEARCH RESOURCES ---");
        System.out.print("  Keyword (name/location): ");
        String keyword = scanner.nextLine().trim();

        try {
            List<Resource> results = resourceService.searchResources(keyword);
            if (results.isEmpty()) {
                System.out.println("  No resources match '" + keyword + "'.");
            } else {
                System.out.println("  Found " + results.size() + " result(s):");
                results.forEach(r -> System.out.println("  " + r.toReportLine()));
            }
        } catch (InvalidInputException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 4. Book Resource
    // -------------------------------------------------------------------------

    private void bookResource(User user) {
        System.out.println("\n--- BOOK A RESOURCE ---");
        try {
            // Show available resources first
            List<Resource> available = resourceService.getAllResources().stream()
                .filter(Resource::isAvailable)
                .toList();
            if (available.isEmpty()) {
                System.out.println("  No resources are currently available for booking.");
                return;
            }
            System.out.println("  Available Resources:");
            available.forEach(r -> System.out.println("  " + r.toReportLine()));

            System.out.print("\n  Resource ID  : ");
            String ridStr = scanner.nextLine().trim();
            int resourceId = Integer.parseInt(ridStr);

            System.out.print("  Date (yyyy-MM-dd): ");
            String date = scanner.nextLine().trim();

            System.out.print("  Start time (HH:mm): ");
            String start = scanner.nextLine().trim();

            System.out.print("  End time   (HH:mm): ");
            String end = scanner.nextLine().trim();

            Booking booking = bookingService.bookResource(user.getUserId(), resourceId, date, start, end);
            System.out.println("[SUCCESS] Booking confirmed! Booking ID: " + booking.getBookingId());

        } catch (BookingConflictException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (InvalidInputException | ResourceNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Resource ID must be a number.");
        } catch (Exception e) {
            System.out.println("[ERROR] Booking failed. Please try again.");
            Logger.error("bookResource: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 5. My Bookings
    // -------------------------------------------------------------------------

    private void viewMyBookings(User user) {
        System.out.println("\n--- MY BOOKINGS ---");
        try {
            List<Booking> bookings = bookingService.getBookingsForUser(user.getUserId());
            if (bookings.isEmpty()) {
                System.out.println("  You have no bookings.");
                return;
            }
            bookings.forEach(b -> System.out.println("  " + b.toReportLine()));
        } catch (Exception e) {
            System.out.println("[ERROR] Could not load bookings.");
            Logger.error("viewMyBookings: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 6. Cancel Booking
    // -------------------------------------------------------------------------

    private void cancelBooking(User user) {
        System.out.println("\n--- CANCEL BOOKING ---");
        try {
            viewMyBookings(user);
            System.out.print("\n  Booking ID to cancel: ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  Confirm cancellation? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes")) {
                System.out.println("  Cancellation aborted.");
                return;
            }

            bookingService.cancelBooking(user.getUserId(), bookingId);
            System.out.println("[SUCCESS] Booking #" + bookingId + " has been cancelled.");

        } catch (InvalidInputException | ResourceNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Booking ID must be a number.");
        } catch (Exception e) {
            System.out.println("[ERROR] Cancellation failed.");
            Logger.error("cancelBooking: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 7. Raise Service Request
    // -------------------------------------------------------------------------

    private void raiseServiceRequest(User user) {
        System.out.println("\n--- RAISE SERVICE REQUEST ---");
        try {
            // Show categories (Unit 1 — array + for loop)
            System.out.println("  Categories:");
            for (int i = 0; i < REQUEST_CATEGORIES.length; i++) {
                System.out.printf("    %d. %s%n", i + 1, REQUEST_CATEGORIES[i]);
            }
            System.out.print("  Category number: ");
            int catIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (catIdx < 0 || catIdx >= REQUEST_CATEGORIES.length) {
                System.out.println("[ERROR] Invalid category selection.");
                return;
            }
            String category = REQUEST_CATEGORIES[catIdx];

            System.out.print("  Description    : ");
            String description = scanner.nextLine().trim();

            System.out.println("  Priority: LOW, MEDIUM, HIGH, CRITICAL");
            System.out.print("  Priority: ");
            String priority = scanner.nextLine().trim();

            System.out.print("  Resource ID (optional, press ENTER to skip): ");
            String ridStr = scanner.nextLine().trim();
            Integer resourceId = ridStr.isEmpty() ? null : Integer.parseInt(ridStr);

            ServiceRequest req = requestService.createRequest(
                user.getUserId(), resourceId, description, category, priority);
            System.out.println("[SUCCESS] Service request #" + req.getRequestId() + " submitted.");

        } catch (InvalidInputException e) {
            System.out.println("[ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("[ERROR] Could not submit request.");
            Logger.error("raiseServiceRequest: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 8. View My Requests
    // -------------------------------------------------------------------------

    private void viewMyRequests(User user) {
        System.out.println("\n--- MY SERVICE REQUESTS ---");
        try {
            List<ServiceRequest> requests = requestService.getRequestsForUser(user.getUserId());
            if (requests.isEmpty()) {
                System.out.println("  You have no service requests.");
                return;
            }
            requests.forEach(r -> System.out.println("  " + r.toReportLine()));
        } catch (Exception e) {
            System.out.println("[ERROR] Could not load service requests.");
            Logger.error("viewMyRequests: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 9. Generate My Report
    // -------------------------------------------------------------------------

    private void generateMyReport(User user) {
        System.out.println("\n--- MY ACTIVITY REPORT ---");
        try {
            List<Booking> bookings = bookingService.getBookingsForUser(user.getUserId());
            List<ServiceRequest> requests = requestService.getRequestsForUser(user.getUserId());

            System.out.println("  Name   : " + user.getName());
            System.out.println("  Email  : " + user.getEmail());
            System.out.printf("  Total Bookings : %d%n", bookings.size());

            // Unit 4 — Streams: count by status
            long confirmed = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
            long cancelled = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
            System.out.printf("    Confirmed: %d | Cancelled: %d%n", confirmed, cancelled);

            System.out.printf("  Service Requests: %d%n", requests.size());

        } catch (Exception e) {
            System.out.println("[ERROR] Could not generate report.");
            Logger.error("generateMyReport: " + e.getMessage(), e);
        }
    }
}
