# System Architecture Document

## 1. High-Level Architectural Pattern

The **Smart Campus Resource & Service Management System** follows a strict, four-tier **Layered Architecture** complemented by the **Data Access Object (DAO)** pattern, **Singleton Pattern**, and **Model-View-Controller (CLI-variant)** separation of concerns.

```
       +-------------------------------------------------------+
       |             Presentation Layer (CLI UI)               |
       |  ConsoleUI  |  StudentMenu  |  AdminMenu  |  Scanner  |
       +-------------------------------------------------------+
                                  │
                                  ▼
       +-------------------------------------------------------+
       |             Service Layer (Business Logic)            |
       |  AuthService | ResourceService | BookingService (Lock)|
       |     ServiceRequestService     |     ReportService     |
       +-------------------------------------------------------+
                                  │
                                  ▼
       +-------------------------------------------------------+
       |           Data Access Object (DAO) Layer              |
       |   UserDAO  |  ResourceDAO  |  BookingDAO  |  AuditDAO |
       |                  ServiceRequestDAO                    |
       +-------------------------------------------------------+
                                  │
                                  ▼
       +-------------------------------------------------------+
       |           Persistence & Infrastructure Layer          |
       |    DatabaseManager (Singleton) | SQLite JDBC (Driver) |
       |         FileUtil (Reports)     | Logger (File I/O)    |
       +-------------------------------------------------------+
```

---

## 2. Layer-by-Layer Responsibilities

### Tier 1: Presentation Layer (`com.campus.ui`)
- **`ConsoleUI.java`:** The primary interactive front controller. Displays ASCII banners, top-level menus (Login, Register, Guest Resource Browse, Exit), and routes authenticated sessions to either `StudentMenu` or `AdminMenu`.
- **`StudentMenu.java`:** Handles the student user experience. Collects user input via `Scanner`, validates entries using `InputValidator`, and delegates business workflows to services.
- **`AdminMenu.java`:** Implements the administrator console. Contains sub-menus for resource management, booking adjudication, service ticket triaging, report generation, audit log review, action history (`Stack`), and the live multithreaded concurrency stress test.

### Tier 2: Service Layer (`com.campus.service`)
- **`AuthService.java`:** Handles user registration, authentication verification using SHA-256 salted hashes (`PasswordUtil`), and active session state.
- **`ResourceService.java`:** Manages resource lifecycle, business validation, and audit recording.
- **`BookingService.java`:** Encapsulates the core scheduling engine. Implements fine-grained per-resource mutex locks (`ConcurrentHashMap<Integer, Object>`) to guarantee atomic check-then-insert execution, preventing double bookings.
- **`ServiceRequestService.java`:** Manages ticket creation, priority escalation, and status resolution workflows.
- **`ReportService.java`:** Queries DAOs and aggregates metrics using Java 8+ Streams (`filter`, `map`, `groupingBy`, `counting`, `sorted`), outputting formatted ASCII summaries and exporting files.
- **`AuditService.java`:** Records system transactions both to the persistent SQLite `audit_logs` table via `AuditDAO` and to an in-memory `Vector<AuditEntry>` for thread-safe live inspection.

### Tier 3: Data Access Object (DAO) Layer (`com.campus.dao`)
- Direct interaction with SQLite JDBC.
- Completely decouples domain models from SQL queries.
- Utilizes `PreparedStatement` exclusively to prevent SQL injection vulnerabilities.
- Handles `ResultSet` cursor traversal and converts relational rows into rich Java Domain Objects.
- Wraps raw `SQLException` instances into unchecked `DatabaseOperationException` to maintain a clean API boundary.

### Tier 4: Utility & Infrastructure Layer (`com.campus.util`)
- **`DatabaseManager.java`:** Thread-safe Singleton managing the open SQLite JDBC connection and foreign-key pragma configuration.
- **`DatabaseInitializer.java`:** Performs idempotent schema bootstrapping (`CREATE TABLE IF NOT EXISTS`) and populates sample seeds on clean startup.
- **`InputValidator.java`:** Static regex and boundary validator for emails, passwords, dates (`yyyy-MM-dd`), 24-hour times (`HH:mm`), and integer ranges.
- **`PasswordUtil.java`:** Cryptographic hashing utility combining SHA-256 with static application salt.
- **`Logger.java`:** Thread-safe logging mechanism writing timestamps, log levels, and messages to `data/logs/application.log`.
- **`FileUtil.java`:** High-level file and directory management for writing and appending reports.

---

## 3. Design Patterns Applied

| Pattern | Implementation File | Purpose in Project |
| :--- | :--- | :--- |
| **Singleton Pattern** | `DatabaseManager.java` | Guarantees a single, centralized database connection handle to prevent SQLite file locking conflicts. |
| **Data Access Object (DAO)** | `com.campus.dao.*` | Isolates all persistence and SQL query logic from the business services. |
| **Dependency Injection (Manual)** | `Main.java` | Decouples instantiation from usage. `Main.java` instantiates DAOs, passes them to Services, and injects Services into UI menus. |
| **Template / Polymorphism** | `User.java`, `Student.java`, `Admin.java` | Base class defines common attributes (`userId`, `name`, `email`, `role`); derived classes override `getDashboardTitle()`. |
| **Mutex / Monitor Pattern** | `BookingService.java` | Uses per-resource lock objects to protect the critical check-and-insert section against concurrent thread access. |
| **Strategy / Functional Interface** | `Reportable.java` | Standardized reporting contract implemented by entity models (`toReportLine()`). |
