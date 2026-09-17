package com.campus.model;

import com.campus.model.enums.BookingStatus;

/**
 * Booking — Represents a resource reservation made by a student.
 *
 * Unit 2 OOP: class, encapsulation, constructor overloading, implements Reportable.
 */
public class Booking implements Reportable {

    private int           bookingId;
    private int           userId;
    private int           resourceId;
    private String        bookingDate;   // yyyy-MM-dd
    private String        startTime;     // HH:mm
    private String        endTime;       // HH:mm
    private BookingStatus status;
    private String        createdAt;

    // Optional denormalized fields for display (populated by joins in DAO)
    private String userName;
    private String resourceName;

    // Constructor for new booking (no ID yet)
    public Booking(int userId, int resourceId, String bookingDate,
                   String startTime, String endTime, BookingStatus status, String createdAt) {
        this.userId      = userId;
        this.resourceId  = resourceId;
        this.bookingDate = bookingDate;
        this.startTime   = startTime;
        this.endTime     = endTime;
        this.status      = status;
        this.createdAt   = createdAt;
    }

    // Constructor for loading from DB (includes bookingId)
    public Booking(int bookingId, int userId, int resourceId,
                   String bookingDate, String startTime, String endTime,
                   BookingStatus status, String createdAt) {
        this(userId, resourceId, bookingDate, startTime, endTime, status, createdAt);
        this.bookingId = bookingId;
    }

    // -------------------------------------------------------------------------
    // Reportable interface
    // -------------------------------------------------------------------------

    @Override
    public String toReportLine() {
        String resDisplay  = (resourceName != null) ? resourceName : "Resource #" + resourceId;
        String userDisplay = (userName != null)     ? userName     : "User #" + userId;
        return String.format("[%d] %-20s | %s | %s-%s | %-12s | User: %s",
                bookingId, resDisplay, bookingDate, startTime, endTime, status, userDisplay);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public int           getBookingId()    { return bookingId; }
    public int           getUserId()       { return userId; }
    public int           getResourceId()   { return resourceId; }
    public String        getBookingDate()  { return bookingDate; }
    public String        getStartTime()    { return startTime; }
    public String        getEndTime()      { return endTime; }
    public BookingStatus getStatus()       { return status; }
    public String        getCreatedAt()    { return createdAt; }
    public String        getUserName()     { return userName; }
    public String        getResourceName() { return resourceName; }

    public void setBookingId(int id)       { this.bookingId = id; }
    public void setStatus(BookingStatus s) { this.status = s; }
    public void setUserName(String name)   { this.userName = name; }
    public void setResourceName(String n)  { this.resourceName = n; }

    @Override
    public String toString() { return toReportLine(); }
}
