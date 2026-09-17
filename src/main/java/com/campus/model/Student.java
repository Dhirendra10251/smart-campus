package com.campus.model;

import com.campus.model.enums.Role;

/**
 * Student — Concrete subclass of User.
 *
 * Unit 2 OOP:
 * - inheritance (extends User)
 * - method overriding (@Override getDashboardTitle)
 * - super() constructor call
 */
public class Student extends User {

    // Student-specific inner statistics (demonstrates inner class use)
    private int totalBookings;
    private int totalRequests;

    // Constructor for new student registration
    public Student(String name, String email, String passwordHash, String createdAt) {
        super(name, email, passwordHash, Role.STUDENT, createdAt);
    }

    // Constructor for loading existing student from DB
    public Student(int userId, String name, String email,
                   String passwordHash, String createdAt) {
        super(userId, name, email, passwordHash, Role.STUDENT, createdAt);
    }

    // -------------------------------------------------------------------------
    // Overridden methods — polymorphism
    // -------------------------------------------------------------------------

    @Override
    public String getDashboardTitle() {
        return "STUDENT DASHBOARD — Welcome, " + getName();
    }

    @Override
    public String getProfileSummary() {
        // Calls parent implementation and appends student-specific info
        return super.getProfileSummary() +
               String.format(" | Bookings: %d | Requests: %d",
                             totalBookings, totalRequests);
    }

    // -------------------------------------------------------------------------
    // Student-specific getters/setters
    // -------------------------------------------------------------------------

    public int getTotalBookings()  { return totalBookings; }
    public int getTotalRequests()  { return totalRequests; }

    public void setTotalBookings(int count) { this.totalBookings = count; }
    public void setTotalRequests(int count) { this.totalRequests = count; }
}
