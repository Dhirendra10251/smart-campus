package com.campus.service;

import com.campus.dao.BookingDAO;
import com.campus.exception.BookingConflictException;
import com.campus.exception.InvalidInputException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.model.Booking;
import com.campus.model.Resource;
import com.campus.model.enums.BookingStatus;
import com.campus.model.enums.ResourceStatus;
import com.campus.util.InputValidator;
import com.campus.util.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BookingService — Core booking business logic with thread-safe conflict prevention.
 *
 * ============================================================
 * CONCURRENCY DESIGN (Unit 3 — Multithreading):
 * ============================================================
 *
 * Problem:
 *   Without synchronization, two threads (T1, T2) can both call
 *   bookResource() for the same resource/slot, both execute the
 *   hasConflict() check and see "no conflict", then both insert
 *   successfully — creating a double booking.
 *
 * Solution:
 *   A ConcurrentHashMap maps each resourceId to a dedicated lock object.
 *   bookResource() synchronizes on that resource's lock before the
 *   check-then-insert sequence. This ensures only ONE thread can
 *   execute the critical section per resource at a time.
 *
 *   ConcurrentHashMap itself is used to safely create/retrieve lock
 *   objects from multiple threads without synchronizing the whole map.
 *   Each resourceId gets its own fine-grained lock, meaning bookings
 *   for DIFFERENT resources proceed in parallel (no unnecessary blocking).
 *
 * Why not synchronize the whole method?
 *   Synchronizing the entire method would serialize ALL booking attempts
 *   across ALL resources, unnecessarily reducing throughput.
 *   Per-resource locks allow true concurrency between different resources.
 * ============================================================
 */
public class BookingService {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BookingDAO     bookingDAO;
    private final ResourceService resourceService;
    private final AuditService   auditService;

    /**
     * Per-resource lock objects.
     * ConcurrentHashMap ensures thread-safe map access;
     * each value is a plain Object used as a mutex.
     *
     * Unit 3 — Synchronization / thread safety.
     */
    private final ConcurrentHashMap<Integer, Object> resourceLocks = new ConcurrentHashMap<>();

    public BookingService(BookingDAO bookingDAO, ResourceService resourceService,
                          AuditService auditService) {
        this.bookingDAO      = bookingDAO;
        this.resourceService = resourceService;
        this.auditService    = auditService;
    }

    // -------------------------------------------------------------------------
    // BOOK RESOURCE — thread-safe critical section
    // -------------------------------------------------------------------------

    /**
     * Attempts to book a resource for a user.
     *
     * Steps:
     *  1. Validate inputs.
     *  2. Verify resource exists and is AVAILABLE.
     *  3. Acquire per-resource lock.
     *  4. Re-check conflict inside the lock.
     *  5. Insert booking.
     *  6. Release lock.
     *
     * @throws InvalidInputException    on bad input
     * @throws ResourceNotFoundException if resource doesn't exist
     * @throws BookingConflictException  if slot is already booked
     */
    public Booking bookResource(int userId, int resourceId,
                                String dateStr, String startStr, String endStr) {

        // --- Step 1: Validate input ---
        LocalDate date  = InputValidator.validateDate(dateStr);
        LocalTime start = InputValidator.validateTime(startStr, "Start time");
        LocalTime end   = InputValidator.validateTime(endStr, "End time");
        InputValidator.validateTimeRange(start, end);

        // --- Step 2: Validate resource ---
        Resource resource = resourceService.getResourceById(resourceId);
        if (resource.getStatus() != ResourceStatus.AVAILABLE) {
            throw new InvalidInputException(
                "Resource '" + resource.getName() + "' is currently " + resource.getStatus() +
                " and cannot be booked.");
        }

        // --- Step 3: Acquire per-resource lock ---
        // computeIfAbsent is atomic in ConcurrentHashMap
        Object lock = resourceLocks.computeIfAbsent(resourceId, k -> new Object());

        synchronized (lock) {
            // --- Step 4: Conflict check INSIDE lock (check-then-act is atomic) ---
            if (bookingDAO.hasConflict(resourceId, dateStr, startStr, endStr, 0)) {
                throw new BookingConflictException(
                    "Booking conflict: '" + resource.getName() +
                    "' is already booked on " + dateStr +
                    " between " + startStr + " and " + endStr + ".");
            }

            // --- Step 5: Insert ---
            String now = LocalDateTime.now().format(DT_FMT);
            Booking booking = new Booking(userId, resourceId, dateStr,
                                          startStr, endStr, BookingStatus.CONFIRMED, now);
            bookingDAO.insert(booking);

            auditService.log(userId, "BOOKING_CREATE",
                    "User #" + userId + " booked '" + resource.getName() +
                    "' on " + dateStr + " " + startStr + "-" + endStr);

            Logger.info("Booking created: ID " + booking.getBookingId() +
                        " for resource #" + resourceId + " by user #" + userId);
            return booking;
        }
        // --- Step 6: Lock released by synchronized block exit ---
    }

    // -------------------------------------------------------------------------
    // CANCEL BOOKING
    // -------------------------------------------------------------------------

    public void cancelBooking(int userId, int bookingId) {
        Booking booking = bookingDAO.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking ID " + bookingId + " not found."));

        if (booking.getUserId() != userId) {
            throw new InvalidInputException("You can only cancel your own bookings.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidInputException("Booking #" + bookingId + " is already cancelled.");
        }

        bookingDAO.updateStatus(bookingId, BookingStatus.CANCELLED);
        auditService.log(userId, "BOOKING_CANCEL",
                "User #" + userId + " cancelled booking #" + bookingId);
    }

    // -------------------------------------------------------------------------
    // ADMIN: update any booking status
    // -------------------------------------------------------------------------

    public void adminUpdateBookingStatus(int adminId, int bookingId, BookingStatus newStatus) {
        bookingDAO.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking ID " + bookingId + " not found."));
        bookingDAO.updateStatus(bookingId, newStatus);
        auditService.log(adminId, "BOOKING_ADMIN_UPDATE",
                "Admin #" + adminId + " set booking #" + bookingId + " to " + newStatus);
    }

    // -------------------------------------------------------------------------
    // QUERY
    // -------------------------------------------------------------------------

    public List<Booking> getBookingsForUser(int userId) {
        return bookingDAO.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public Booking getBookingById(int bookingId) {
        return bookingDAO.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking ID " + bookingId + " not found."));
    }

    // -------------------------------------------------------------------------
    // Expose DAO for tests that need direct conflict checking
    // -------------------------------------------------------------------------

    public BookingDAO getBookingDAO() { return bookingDAO; }
}
