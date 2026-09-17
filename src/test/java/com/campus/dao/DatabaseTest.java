package com.campus.dao;

import com.campus.model.*;
import com.campus.model.enums.*;
import com.campus.util.DatabaseInitializer;
import com.campus.util.DatabaseManager;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database and DAO Layer Integration Tests")
class DatabaseTest {

    private static final String TEST_DB_FILE = "data/test_campus.db";
    private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

    private UserDAO userDAO;
    private ResourceDAO resourceDAO;
    private BookingDAO bookingDAO;
    private ServiceRequestDAO requestDAO;
    private AuditDAO auditDAO;

    @BeforeEach
    void setUp() {
        // Reset DB to test file
        File f = new File(TEST_DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        DatabaseManager.setDatabaseUrl(TEST_DB_URL);
        DatabaseInitializer.initialize();

        userDAO = new UserDAO();
        resourceDAO = new ResourceDAO();
        bookingDAO = new BookingDAO();
        requestDAO = new ServiceRequestDAO();
        auditDAO = new AuditDAO();
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
    @DisplayName("UserDAO CRUD operations")
    void testUserCrud() {
        // Seed users exist
        Optional<User> admin = userDAO.findByEmail("admin@campus.edu");
        assertTrue(admin.isPresent());
        assertEquals(Role.ADMIN, admin.get().getRole());

        // Insert new student
        Student student = new Student("Charlie Brown", "charlie@campus.edu", "hashed_pwd", "2026-09-17 10:00:00");
        int id = userDAO.insert(student);
        assertTrue(id > 0);

        Optional<User> fetched = userDAO.findById(id);
        assertTrue(fetched.isPresent());
        assertEquals("Charlie Brown", fetched.get().getName());

        // Update role
        userDAO.updateRole(id, Role.ADMIN);
        Optional<User> updated = userDAO.findById(id);
        assertTrue(updated.isPresent());
        assertEquals(Role.ADMIN, updated.get().getRole());

        // Delete
        userDAO.delete(id);
        assertFalse(userDAO.findById(id).isPresent());
    }

    @Test
    @DisplayName("ResourceDAO CRUD operations")
    void testResourceCrud() {
        List<Resource> resources = resourceDAO.findAll();
        assertFalse(resources.isEmpty());

        Resource res = new Resource("Robotics Lab", ResourceType.LAB, "Block D", 25, ResourceStatus.AVAILABLE, "2026-09-17 10:00:00");
        int id = resourceDAO.insert(res);
        assertTrue(id > 0);

        Optional<Resource> fetched = resourceDAO.findById(id);
        assertTrue(fetched.isPresent());
        assertEquals("Robotics Lab", fetched.get().getName());

        // Update status
        resourceDAO.updateStatus(id, ResourceStatus.UNDER_MAINTENANCE);
        assertEquals(ResourceStatus.UNDER_MAINTENANCE, resourceDAO.findById(id).get().getStatus());

        // Delete
        resourceDAO.delete(id);
        assertFalse(resourceDAO.findById(id).isPresent());
    }

    @Test
    @DisplayName("BookingDAO conflict detection and status updates")
    void testBookingOperations() {
        // Seed bookings are present
        List<Booking> bookings = bookingDAO.findAll();
        assertFalse(bookings.isEmpty());

        // Check conflict on Computer Lab A (id 1) which is booked 2026-09-10 from 09:00 to 11:00
        boolean hasConflict = bookingDAO.hasConflict(1, "2026-09-10", "10:00", "12:00", 0);
        assertTrue(hasConflict, "Overlapping slot must report conflict");

        boolean noConflict = bookingDAO.hasConflict(1, "2026-09-10", "11:00", "13:00", 0);
        assertFalse(noConflict, "Non-overlapping slot should not report conflict");

        // Insert new booking
        Booking newBooking = new Booking(2, 1, "2026-10-01", "10:00", "11:00", BookingStatus.CONFIRMED, "2026-09-17 10:00:00");
        int bId = bookingDAO.insert(newBooking);
        assertTrue(bId > 0);

        // Update status to CANCELLED
        bookingDAO.updateStatus(bId, BookingStatus.CANCELLED);
        assertEquals(BookingStatus.CANCELLED, bookingDAO.findById(bId).get().getStatus());
    }

    @Test
    @DisplayName("ServiceRequestDAO operations")
    void testServiceRequestOperations() {
        List<ServiceRequest> requests = requestDAO.findAll();
        assertFalse(requests.isEmpty());

        ServiceRequest req = new ServiceRequest(2, 1, "Broken projector screen", "AV", Priority.HIGH, RequestStatus.OPEN, "2026-09-17 10:00:00", "2026-09-17 10:00:00");
        int rId = requestDAO.insert(req);
        assertTrue(rId > 0);

        requestDAO.updateStatus(rId, RequestStatus.RESOLVED, "2026-09-17 11:00:00");
        assertEquals(RequestStatus.RESOLVED, requestDAO.findById(rId).get().getStatus());

        requestDAO.updatePriority(rId, Priority.CRITICAL, "2026-09-17 11:30:00");
        assertEquals(Priority.CRITICAL, requestDAO.findById(rId).get().getPriority());
    }

    @Test
    @DisplayName("AuditDAO logging and query")
    void testAuditDAO() {
        AuditEntry entry = new AuditEntry(1, "TEST_ACTION", "Details about test", "2026-09-17 10:00:00");
        auditDAO.insert(entry);

        List<AuditEntry> entries = auditDAO.findAll();
        assertFalse(entries.isEmpty());
        assertTrue(entries.stream().anyMatch(e -> "TEST_ACTION".equals(e.getAction())));
    }
}
