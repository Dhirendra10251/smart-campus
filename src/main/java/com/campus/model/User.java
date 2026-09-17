package com.campus.model;

import com.campus.model.enums.Role;

/**
 * User — Abstract base class for all system users.
 *
 * Unit 2 OOP demonstrations:
 * - abstract class
 * - encapsulation (private fields + getters)
 * - abstract method getDashboardTitle() — forces subclass-specific behaviour
 * - constructor (two variants: with/without userId for new vs. loaded users)
 */
public abstract class User {

    // Private fields — encapsulation
    private int    userId;
    private String name;
    private String email;
    private String passwordHash;  // never printed; stored hashed
    private Role   role;
    private String createdAt;

    // -------------------------------------------------------------------------
    // Constructors — constructor overloading
    // -------------------------------------------------------------------------

    /** Used when creating a new user (userId not yet known). */
    public User(String name, String email, String passwordHash, Role role, String createdAt) {
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.createdAt    = createdAt;
    }

    /** Used when loading an existing user from the database. */
    public User(int userId, String name, String email,
                String passwordHash, Role role, String createdAt) {
        this(name, email, passwordHash, role, createdAt); // calls sibling constructor (super chaining)
        this.userId = userId;
    }

    // -------------------------------------------------------------------------
    // Abstract method — subclasses must implement
    // -------------------------------------------------------------------------

    /**
     * Returns the title shown at the top of the user's dashboard menu.
     * Demonstrates polymorphism — different behaviour per subclass.
     */
    public abstract String getDashboardTitle();

    /**
     * Returns a formatted summary of the user's profile.
     * Demonstrates a concrete method that subclasses may override (open/closed).
     */
    public String getProfileSummary() {
        return String.format(
            "ID: %d | Name: %s | Email: %s | Role: %s | Joined: %s",
            userId, name, email, role, createdAt);
    }

    // -------------------------------------------------------------------------
    // Getters (no setters for immutable identity fields; password never exposed)
    // -------------------------------------------------------------------------

    public int    getUserId()       { return userId; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role   getRole()         { return role; }
    public String getCreatedAt()    { return createdAt; }

    // Setter only for userId (set after DB insert returns generated key)
    public void setUserId(int userId) { this.userId = userId; }
    public void setName(String name)  { this.name = name; }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', role=%s}",
                             userId, name, email, role);
    }
}
