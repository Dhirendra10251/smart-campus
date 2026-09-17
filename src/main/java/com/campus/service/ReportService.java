package com.campus.service;

import com.campus.model.Booking;
import com.campus.model.Resource;
import com.campus.model.ServiceRequest;
import com.campus.model.enums.BookingStatus;
import com.campus.model.enums.Priority;
import com.campus.model.enums.RequestStatus;
import com.campus.model.enums.ResourceStatus;
import com.campus.model.enums.ResourceType;
import com.campus.util.FileUtil;
import com.campus.util.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportService — Generates analytics reports using Java Streams.
 *
 * ============================================================
 * Unit 4 — Java Streams demonstration:
 * ============================================================
 *  - filter()      : select only confirmed bookings, open requests, etc.
 *  - map()         : transform objects to display strings
 *  - collect()     : to List, to Map via groupingBy, counting
 *  - groupingBy()  : group bookings by resource, group requests by priority
 *  - counting()    : aggregate counts per group
 *  - sorted()      : sort resources by booking count
 *  - Collectors.joining() : concatenate report lines
 * ============================================================
 *
 * Reports are returned as formatted strings and optionally exported to files.
 */
public class ReportService {

    private static final String REPORTS_DIR = "data/reports";
    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEP = "=".repeat(70) + "\n";

    private final ResourceService        resourceService;
    private final BookingService         bookingService;
    private final ServiceRequestService  requestService;

    public ReportService(ResourceService resourceService,
                         BookingService bookingService,
                         ServiceRequestService requestService) {
        this.resourceService = resourceService;
        this.bookingService  = bookingService;
        this.requestService  = requestService;
    }

    // -------------------------------------------------------------------------
    // RESOURCE REPORT
    // -------------------------------------------------------------------------

    public String generateResourceReport() {
        List<Resource> resources = resourceService.getAllResources();

        // Streams: groupingBy type, counting
        Map<ResourceType, Long> byType = resources.stream()
            .collect(Collectors.groupingBy(Resource::getType, Collectors.counting()));

        // Streams: groupingBy status, counting
        Map<ResourceStatus, Long> byStatus = resources.stream()
            .collect(Collectors.groupingBy(Resource::getStatus, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append(SEP);
        sb.append("  RESOURCE REPORT\n");
        sb.append("  Generated: ").append(LocalDateTime.now().format(DT_FMT)).append("\n");
        sb.append(SEP);
        sb.append(String.format("  Total Resources : %d%n", resources.size()));
        sb.append("\n  BY TYPE:\n");
        byType.forEach((type, count) ->
            sb.append(String.format("    %-20s : %d%n", type.getDisplayName(), count)));
        sb.append("\n  BY STATUS:\n");
        byStatus.forEach((status, count) ->
            sb.append(String.format("    %-20s : %d%n", status, count)));
        sb.append("\n  RESOURCE LISTING:\n");

        // Streams: map to report lines and join
        String listing = resources.stream()
            .map(Resource::toReportLine)
            .collect(Collectors.joining("\n  ", "  ", "\n"));
        sb.append(listing);
        sb.append(SEP);

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // BOOKING REPORT
    // -------------------------------------------------------------------------

    public String generateBookingReport() {
        List<Booking> bookings = bookingService.getAllBookings();

        // Streams: count by status
        Map<BookingStatus, Long> byStatus = bookings.stream()
            .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

        // Streams: confirmed bookings grouped by resource name
        Map<String, Long> byResource = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .collect(Collectors.groupingBy(
                b -> (b.getResourceName() != null ? b.getResourceName() : "Resource #" + b.getResourceId()),
                Collectors.counting()));

        // Streams: find most booked resource
        String mostBooked = byResource.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(e -> e.getKey() + " (" + e.getValue() + " confirmed bookings)")
            .findFirst()
            .orElse("N/A");

        // Streams: confirmed bookings by user
        Map<String, Long> byUser = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .collect(Collectors.groupingBy(
                b -> (b.getUserName() != null ? b.getUserName() : "User #" + b.getUserId()),
                Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append(SEP);
        sb.append("  BOOKING REPORT\n");
        sb.append("  Generated: ").append(LocalDateTime.now().format(DT_FMT)).append("\n");
        sb.append(SEP);
        sb.append(String.format("  Total Bookings  : %d%n", bookings.size()));
        sb.append("\n  BY STATUS:\n");
        byStatus.forEach((status, count) ->
            sb.append(String.format("    %-12s : %d%n", status, count)));
        sb.append(String.format("%n  Most Booked Resource : %s%n", mostBooked));
        sb.append("\n  CONFIRMED BOOKINGS BY RESOURCE:\n");
        byResource.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> sb.append(String.format("    %-30s : %d%n", e.getKey(), e.getValue())));
        sb.append("\n  CONFIRMED BOOKINGS BY USER:\n");
        byUser.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> sb.append(String.format("    %-30s : %d%n", e.getKey(), e.getValue())));
        sb.append("\n  ALL BOOKINGS:\n");
        bookings.stream()
            .map(b -> "  " + b.toReportLine())
            .forEach(line -> sb.append(line).append("\n"));
        sb.append(SEP);

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // SERVICE REQUEST REPORT
    // -------------------------------------------------------------------------

    public String generateServiceRequestReport() {
        List<ServiceRequest> requests = requestService.getAllRequests();

        // Streams: groupingBy status
        Map<RequestStatus, Long> byStatus = requests.stream()
            .collect(Collectors.groupingBy(ServiceRequest::getStatus, Collectors.counting()));

        // Streams: groupingBy priority
        Map<Priority, Long> byPriority = requests.stream()
            .collect(Collectors.groupingBy(ServiceRequest::getPriority, Collectors.counting()));

        // Streams: filter unresolved
        long unresolved = requests.stream()
            .filter(r -> r.getStatus() == RequestStatus.OPEN
                      || r.getStatus() == RequestStatus.IN_PROGRESS)
            .count();

        StringBuilder sb = new StringBuilder();
        sb.append(SEP);
        sb.append("  SERVICE REQUEST REPORT\n");
        sb.append("  Generated: ").append(LocalDateTime.now().format(DT_FMT)).append("\n");
        sb.append(SEP);
        sb.append(String.format("  Total Requests  : %d%n", requests.size()));
        sb.append(String.format("  Unresolved      : %d%n", unresolved));
        sb.append("\n  BY STATUS:\n");
        byStatus.forEach((status, count) ->
            sb.append(String.format("    %-15s : %d%n", status, count)));
        sb.append("\n  BY PRIORITY:\n");
        byPriority.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getKey().getLevel(), a.getKey().getLevel()))
            .forEach(e -> sb.append(String.format("    %-10s (level %d) : %d%n",
                e.getKey(), e.getKey().getLevel(), e.getValue())));
        sb.append("\n  ALL REQUESTS:\n");
        requests.stream()
            .map(r -> "  " + r.toReportLine())
            .forEach(line -> sb.append(line).append("\n"));
        sb.append(SEP);

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // EXPORT
    // -------------------------------------------------------------------------

    /**
     * Exports a report string to data/reports/<filename>.
     */
    public void exportReport(String reportContent, String filename) {
        FileUtil.createDirIfNeeded(REPORTS_DIR);
        String path = REPORTS_DIR + "/" + filename;
        FileUtil.writeToFile(path, reportContent);
        System.out.println("[INFO] Report exported to: " + path);
        Logger.info("Report exported: " + path);
    }

    public void exportResourceReport() {
        exportReport(generateResourceReport(), "resource_report.txt");
    }

    public void exportBookingReport() {
        exportReport(generateBookingReport(), "booking_report.txt");
    }

    public void exportServiceRequestReport() {
        exportReport(generateServiceRequestReport(), "service_request_report.txt");
    }
}
