package com.campus.service;

import com.campus.dao.*;
import com.campus.util.DatabaseInitializer;
import com.campus.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportService Stream Aggregation and Export Tests")
class ReportServiceTest {

    private static final String TEST_DB_FILE = "data/test_report_service.db";
    private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        File f = new File(TEST_DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        DatabaseManager.setDatabaseUrl(TEST_DB_URL);
        DatabaseInitializer.initialize();

        AuditDAO auditDAO = new AuditDAO();
        AuditService auditService = new AuditService(auditDAO);
        ResourceDAO resourceDAO = new ResourceDAO();
        ResourceService resourceService = new ResourceService(resourceDAO, auditService);
        BookingDAO bookingDAO = new BookingDAO();
        BookingService bookingService = new BookingService(bookingDAO, resourceService, auditService);
        ServiceRequestDAO requestDAO = new ServiceRequestDAO();
        ServiceRequestService requestService = new ServiceRequestService(requestDAO, auditService);

        reportService = new ReportService(resourceService, bookingService, requestService);
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.getInstance().closeConnection();
        File f = new File(TEST_DB_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    @AfterAll
    static void resetDatabase() {
        DatabaseManager.resetToDefaultUrl();
    }

    @Test
    @DisplayName("Generate resource report using Java Streams")
    void testGenerateResourceReport() {
        String report = reportService.generateResourceReport();

        assertNotNull(report);
        assertTrue(report.contains("RESOURCE REPORT"));
        assertTrue(report.contains("Total Resources"));
        assertTrue(report.contains("BY TYPE:"));
        assertTrue(report.contains("BY STATUS:"));
        assertTrue(report.contains("Computer Lab A"));
    }

    @Test
    @DisplayName("Generate booking report using Java Streams")
    void testGenerateBookingReport() {
        String report = reportService.generateBookingReport();

        assertNotNull(report);
        assertTrue(report.contains("BOOKING REPORT"));
        assertTrue(report.contains("Total Bookings"));
        assertTrue(report.contains("Most Booked Resource"));
        assertTrue(report.contains("CONFIRMED BOOKINGS BY RESOURCE:"));
    }

    @Test
    @DisplayName("Generate service request report using Java Streams")
    void testGenerateServiceRequestReport() {
        String report = reportService.generateServiceRequestReport();

        assertNotNull(report);
        assertTrue(report.contains("SERVICE REQUEST REPORT"));
        assertTrue(report.contains("Total Requests"));
        assertTrue(report.contains("BY STATUS:"));
        assertTrue(report.contains("BY PRIORITY:"));
    }

    @Test
    @DisplayName("Export report writes file to disk")
    void testExportReport() {
        String dummyContent = "Test Export Content\nLine 2";
        String filename = "test_export.txt";
        reportService.exportReport(dummyContent, filename);

        File exported = new File("data/reports/" + filename);
        assertTrue(exported.exists(), "Exported file should exist");
        assertTrue(exported.length() > 0, "Exported file should not be empty");

        // Clean up
        exported.delete();
    }
}
