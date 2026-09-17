package com.campus.service;

import com.campus.dao.UserDAO;
import com.campus.exception.InvalidInputException;
import com.campus.exception.InvalidLoginException;
import com.campus.model.Student;
import com.campus.model.User;
import com.campus.model.enums.Role;
import com.campus.util.InputValidator;
import com.campus.util.PasswordUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AuthService — Handles registration, login, and session management.
 *
 * Unit 4 — HashMap usage:
 * emailToUser is an in-memory cache for fast O(1) email-based lookup,
 * avoiding a DB query every time the login form is used.
 * (In this CLI app the cache is loaded lazily; its primary value is
 * demonstrating HashMap with a justified reason.)
 *
 * Unit 3 — Exception handling: throws/catches InvalidLoginException and InvalidInputException.
 */
public class AuthService {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserDAO    userDAO;
    private final AuditService auditService;

    /**
     * In-memory email → User cache (HashMap — Unit 4).
     * Populated on first login lookup to avoid repeated DB hits.
     */
    private final Map<String, User> emailCache = new HashMap<>();

    // Currently logged-in user for this session
    private User currentUser;

    public AuthService(UserDAO userDAO, AuditService auditService) {
        this.userDAO      = userDAO;
        this.auditService = auditService;
    }

    // -------------------------------------------------------------------------
    // REGISTER
    // -------------------------------------------------------------------------

    /**
     * Registers a new student account.
     *
     * @throws InvalidInputException if any field is invalid
     * @throws InvalidLoginException if email already in use
     */
    public User register(String name, String email, String password) {
        // Validate inputs — throws InvalidInputException on failure
        name     = InputValidator.validateNonEmpty(name, "Name");
        email    = InputValidator.validateEmail(email);
        InputValidator.validatePassword(password);

        // Check for duplicate email in DB
        if (userDAO.findByEmail(email).isPresent()) {
            throw new InvalidLoginException("An account with this email already exists: " + email);
        }

        String now      = LocalDateTime.now().format(DT_FMT);
        String passHash = PasswordUtil.hashPassword(password);

        Student student = new Student(name, email, passHash, now);
        userDAO.insert(student);

        // Invalidate cache entry if it existed
        emailCache.remove(email);

        auditService.log(student.getUserId(), "REGISTER",
                "New student registered: " + name + " (" + email + ")");

        return student;
    }

    // -------------------------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user and sets the session.
     *
     * @throws InvalidLoginException on wrong credentials
     */
    public User login(String email, String password) {
        email = InputValidator.validateEmail(email);

        // Check HashMap cache first (Unit 4 — HashMap fast lookup)
        User user = emailCache.get(email);
        if (user == null) {
            // Cache miss — query DB
            Optional<User> opt = userDAO.findByEmail(email);
            if (opt.isEmpty()) {
                throw new InvalidLoginException("No account found with email: " + email);
            }
            user = opt.get();
            emailCache.put(email, user); // populate cache
        }

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            auditService.log(null, "LOGIN_FAIL", "Failed login attempt for: " + email);
            throw new InvalidLoginException("Incorrect password.");
        }

        currentUser = user;
        auditService.log(user.getUserId(), "LOGIN", user.getName() + " logged in.");
        return user;
    }

    // -------------------------------------------------------------------------
    // LOGOUT
    // -------------------------------------------------------------------------

    public void logout() {
        if (currentUser != null) {
            auditService.log(currentUser.getUserId(), "LOGOUT",
                    currentUser.getName() + " logged out.");
            currentUser = null;
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public User getCurrentUser()    { return currentUser; }
    public boolean isLoggedIn()     { return currentUser != null; }
    public boolean isAdmin()        { return isLoggedIn() && currentUser.getRole() == Role.ADMIN; }
}
