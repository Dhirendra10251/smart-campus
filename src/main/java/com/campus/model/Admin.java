package com.campus.model;

import com.campus.model.enums.Role;

/**
 * Admin — Concrete subclass of User.
 *
 * Unit 2 OOP:
 * - inheritance (extends User)
 * - method overriding (@Override getDashboardTitle)
 * - super() constructor call
 */
public class Admin extends User {

    // Constructor for loading existing admin from DB
    public Admin(int userId, String name, String email,
                 String passwordHash, String createdAt) {
        super(userId, name, email, passwordHash, Role.ADMIN, createdAt);
    }

    // Constructor for creating a new admin account
    public Admin(String name, String email, String passwordHash, String createdAt) {
        super(name, email, passwordHash, Role.ADMIN, createdAt);
    }

    // -------------------------------------------------------------------------
    // Overridden methods — polymorphism
    // -------------------------------------------------------------------------

    @Override
    public String getDashboardTitle() {
        return "ADMIN DASHBOARD — " + getName();
    }

    @Override
    public String getProfileSummary() {
        return super.getProfileSummary() + " | [ADMINISTRATOR]";
    }
}
