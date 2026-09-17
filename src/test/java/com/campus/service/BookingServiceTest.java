package com.campus.service;

import com.campus.dao.*;
import com.campus.exception.BookingConflictException;
import com.campus.exception.InvalidInputException;
import com.campus.model.Booking;
import com.campus.model.enums.BookingStatus;
import com.campus.model.enums.ResourceStatus;
import com.campus.util.DatabaseInitializer;
import com.campus.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingService & Concurrency Tests")
class BookingServiceTest {

    private static final String TEST_DB_FILE = "data/test_booking_service.db";
    private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

    private ResourceService resourceService;
    private BookingService bookingService;
    private AuditService auditService;
    private BookingDAO bookingDAO;
    private ResourceDAO resourceDAO;

    @BeforeEach
    void setUp() {
        File f = new File(TEST_DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        DatabaseManager.setDatabaseUrl(TEST_DB_URL);
        DatabaseInitializer.initialize();

        AuditDAO auditDAO = new AuditDAO();
        auditService = new AuditService(auditDAO);
        resourceDAO = new ResourceDAO();
        resourceService = new ResourceService(resourceDAO, auditService);
        bookingDAO = new BookingDAO();
        bookingService = new BookingService(bookingDAO, resourceService, auditService);
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
    @DisplayName("Successful resource booking")
    void testBookResource_Success() {
        String bookingDate = LocalDate.now().plusDays(5).toString();
        Booking booking = bookingService.bookResource(2, 1, bookingDate, "14:00", "15:30");

        assertNotNull(booking);
        assertTrue(booking.getBookingId() > 0);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("14:00", booking.getStartTime());
        assertEquals("15:30", booking.getEndTime());
    }

    @Test
    @DisplayName("Conflict when booking overlapping slot on same resource")
    void testBookResource_Conflict() {
        String bookingDate = LocalDate.now().plusDays(6).toString();
        // First booking succeeds
        bookingService.bookResource(2, 1, bookingDate, "10:00", "12:00");

        // Overlapping booking fails
        assertThrows(BookingConflictException.class, () ->
            bookingService.bookResource(3, 1, bookingDate, "11:00", "13:00"));

        // Exact same slot fails
        assertThrows(BookingConflictException.class, () ->
            bookingService.bookResource(3, 1, bookingDate, "10:00", "12:00"));

        // Enclosing slot fails
        assertThrows(BookingConflictException.class, () ->
            bookingService.bookResource(3, 1, bookingDate, "09:00", "13:00"));

        // Adjacent non-overlapping slot succeeds
        assertDoesNotThrow(() ->
            bookingService.bookResource(3, 1, bookingDate, "12:00", "14:00"));
    }

    @Test
    @DisplayName("Reject booking on resource under maintenance")
    void testBookResource_UnderMaintenance() {
        // Resource #10 (Projector Set 2) is seeded with UNDER_MAINTENANCE
        String bookingDate = LocalDate.now().plusDays(3).toString();
        assertThrows(InvalidInputException.class, () ->
            bookingService.bookResource(2, 10, bookingDate, "09:00", "11:00"));
    }

    @Test
    @DisplayName("Cancel booking updates status")
    void testCancelBooking() {
        String bookingDate = LocalDate.now().plusDays(7).toString();
        Booking booking = bookingService.bookResource(2, 1, bookingDate, "09:00", "10:00");

        bookingService.cancelBooking(2, booking.getBookingId());

        Booking cancelled = bookingService.getBookingById(booking.getBookingId());
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("Multithreaded booking concurrency test (thread-safe synchronization)")
    void testConcurrentBookings() throws InterruptedException {
        int threadCount = 10;
        String bookingDate = LocalDate.now().plusDays(10).toString();
        String startTime = "15:00";
        String endTime = "17:00";
        int resourceId = 3; // Seminar Hall 101

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherErrors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userId = 2; // Alice
            new Thread(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // wait for all threads to start simultaneously
                    bookingService.bookResource(userId, resourceId, bookingDate, startTime, endTime);
                    successCount.incrementAndGet();
                } catch (BookingConflictException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    otherErrors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        readyLatch.await();
        startLatch.countDown(); // Unleash all threads at the exact same instant
        doneLatch.await();

        assertEquals(0, otherErrors.get(), "No unexpected exceptions should occur");
        assertEquals(1, successCount.get(), "Exactly one thread must succeed in booking the slot");
        assertEquals(threadCount - 1, conflictCount.get(), "All other threads must receive BookingConflictException");
    }
}
