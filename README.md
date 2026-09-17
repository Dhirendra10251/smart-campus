# Smart Campus Resource & Service Management System

> **A CLI, Modular Java Application for the VITyarthi Flipped Course Evaluation**  
> *Developed with Java 17+, Maven, SQLite (JDBC), and JUnit 5.*

---

## 📌 Project Overview

The **Smart Campus Resource & Service Management System** is a modular, production-ready Command-Line Java application designed to streamline collegiate campus logistics. In modern universities, students frequently face friction when booking shared facilities (computer labs, seminar halls, sports complexes, projection equipment) and tracking service or maintenance requests. 

This project delivers an end-to-end solution featuring:
- **Role-Based Authentication** for Students and Campus Administrators.
- **Resource Management & Real-Time Availability Checking**.
- **Thread-Safe Resource Booking** with granular per-resource mutex locking to prevent double-booking race conditions.
- **Service & Maintenance Ticketing** with status transitions and priority escalation.
- **Automated Analytics & Report Generation** leveraging the Java Streams API (`filter`, `map`, `groupingBy`, `counting`, `sorted`).
- **File I/O & Audit Logging** for auditing actions, logging runtime telemetry, and exporting persistent text reports.
- **Strict Layered Architecture** (Presentation CLI, Service/Business Logic, Data Access Object [DAO], Database/Storage).

---

## 🏛️ Syllabus Coverage (Units 1 – 5)

This project has been intentionally architected to demonstrate comprehensive mastery of the university Java curriculum:

| Unit | Topic Area | Practical Project Implementations |
| :--- | :--- | :--- |
| **Unit 1** | **Java Basics & OOP** | Abstract base class `User`, concrete subclasses `Student` and `Admin`, `Reportable` interface, encapsulation, method overriding (`getDashboardTitle`), polymorphic menus. |
| **Unit 2** | **Packages, Exceptions & Strings** | Clean package hierarchy (`com.campus.*`), custom exceptions (`BookingConflictException`, `InvalidInputException`, `ResourceNotFoundException`, etc.), try-with-resources, Regex input validation, StringBuilder formatting. |
| **Unit 3** | **Multithreading & Concurrency** | Dedicated `BookingConcurrencyDemo`, fine-grained per-resource mutex locks using `ConcurrentHashMap<Integer, Object>`, `synchronized` check-then-act critical section, `CountDownLatch` stress testing. |
| **Unit 4** | **Collections, Generics & Streams** | `ArrayList` for dynamic records, `HashMap` for O(1) lookups, thread-safe `Vector` for live audit log cache, `Stack` for Admin action history / undo stack, Generics (`Optional<T>`, `validateEnum<T>`), Java Streams API for aggregation & report generation. |
| **Unit 5** | **JDBC & File I/O** | SQLite JDBC integration, 100% parameterised queries using `PreparedStatement`, `ResultSet` mapping, idempotent schema migration, file-based logging (`application.log`), exported analytics reports (`data/reports/`). |

*(For full file-by-file curriculum mapping, see [`docs/syllabus-mapping.md`](docs/syllabus-mapping.md).)*

---

## 🛠️ Technology Stack & Prerequisites

- **Language:** Java 17 or higher (tested with Oracle OpenJDK 26 / 17 LTS)
- **Build System:** Apache Maven 3.6+ (Maven Wrapper `./mvnw` or `mvnw.cmd` included)
- **Database Engine:** SQLite 3.45 via `org.xerial:sqlite-jdbc`
- **Testing Framework:** JUnit 5 (Jupiter API, Engine, and Params)
- **Architecture:** Layered Architecture (CLI UI ➔ Service Layer ➔ DAO Layer ➔ SQLite JDBC)

---

## 🚀 Getting Started

### 1. Clone or Navigate to the Project Root
```bash
cd "c:\Users\Dhiren\OneDrive\Desktop\JAVA"
```

### 2. Compile the Application
Using the bundled Maven Wrapper:
```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"  # Or your local JDK directory
.\mvnw.cmd clean compile
```
Or with standard Maven:
```bash
mvn clean compile
```

### 3. Run the Automated Test Suite (37 Unit & Integration Tests)
```powershell
.\mvnw.cmd test
```
All 37 tests covering input validation, DAO CRUD, service logic, report generation, and multithreaded concurrency stress tests will execute.

### 4. Launch the Application
```powershell
.\mvnw.cmd exec:java
```
Or execute directly using Java from the compiled classes:
```powershell
java -cp "target/classes;target/dependency/*" com.campus.Main
```

---

## 🔑 Pre-Seeded Demonstration Accounts

Upon first launch, the application automatically initializes the SQLite database schema at `data/campus.db` and seeds the following accounts and resources:

| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| **ADMIN** | Admin User | `admin@campus.edu` | `admin123` |
| **STUDENT** | Alice Johnson | `alice@campus.edu` | `student123` |
| **STUDENT** | Bob Smith | `bob@campus.edu` | `student123` |

*Note: You can also register new student accounts directly through the CLI registration menu.*

---

## 🖥️ Live Demo Walkthrough Guide

### Step 1: Browse Resources as a Guest
- From the Main Menu, choose option `[3] Browse Resources`.
- Filter by type (`LAB`, `CLASSROOM`, `SEMINAR_HALL`, `SPORTS_FACILITY`, `EQUIPMENT`) to review initial capacity and status.

### Step 2: Student Experience (Alice)
1. Select `[1] Login` and enter `alice@campus.edu` / `student123`.
2. Notice the polymorphic greeting: `=== Welcome Alice Johnson [STUDENT] ===`.
3. Select `[2] Book a Resource`:
   - Resource ID: `1` (Computer Lab A)
   - Date: Future date (e.g., `2026-10-15`)
   - Time: `14:00` to `16:00`
4. Try booking the **exact same slot again** to witness the `BookingConflictException` and conflict handling.
5. Select `[4] Raise a Service Request`:
   - Enter category `HARDWARE`, priority `HIGH`, and description `"Monitor flickering at workstation 4"`.
6. Select `[6] My Personal Summary`:
   - Review live Stream aggregations summarizing Alice's confirmed bookings and active requests.
7. Logout.

### Step 3: Administrator Experience (Admin)
1. Select `[1] Login` and enter `admin@campus.edu` / `admin123`.
2. Manage Resources (`[1]`): Add, update status (e.g. set a lab to `UNDER_MAINTENANCE`), or delete a facility.
3. Manage Bookings (`[2]`): Inspect all campus bookings, cancel or reject reservations.
4. Manage Service Requests (`[3]`): Transition Alice's request from `OPEN` to `IN_PROGRESS` or `RESOLVED`.
5. **Run Multithreading Concurrency Demo (`[4]`):**
   - Spawns 5 threads concurrently attempting to book Seminar Hall 101 on the same date and time.
   - Observe live console logging proving that exactly ONE thread succeeds while the remaining 4 threads are safely intercepted by `BookingConflictException`.
6. System Reports & Export (`[5]`):
   - Generate Resource, Booking, and Service Request summary reports.
   - Select Export to save formatted text files to `data/reports/`.
7. Audit Log (`[6]`): Review chronological system activities stored in both the database and the in-memory `Vector`.
8. Action History (`[7]`): View recent administrative operations pushed onto the `Stack`.

---

## 📂 Project Structure

```
smart-campus/
├── pom.xml                                 # Maven dependencies, plugins, and build lifecycle
├── mvnw / mvnw.cmd                         # Portable Maven wrapper scripts
├── .mvn/                                   # Maven wrapper configuration
├── .gitignore                              # Git exclusion rules
├── README.md                               # Primary project overview & user guide
├── statement.md                            # Academic problem statement & evaluation scope
├── data/                                   # Runtime directory (auto-created)
│   ├── campus.db                           # SQLite persistent database
│   ├── logs/application.log                # Production audit & runtime telemetry log
│   └── reports/                            # Exported text analytics reports
├── docs/                                   # Architectural & Technical Documentation
│   ├── architecture.md                     # Layered design, MVC-CLI, design patterns
│   ├── database.md                         # Database schema, table specs, seed data
│   ├── syllabus-mapping.md                 # Detailed Units 1-5 curriculum syllabus map
│   ├── design-decisions.md                 # Technical trade-offs and design rationale
│   ├── testing.md                          # Testing methodology & test case matrix
│   └── diagrams/                           # Mermaid visual diagrams
│       ├── use-case.md                     # Actor and use-case diagram
│       ├── class-diagram.md                # Comprehensive class & interface diagram
│       ├── sequence-diagram.md             # Thread-safe booking sequence diagram
│       ├── workflow.md                     # Activity / system workflow diagram
│       └── er-diagram.md                   # Entity-Relationship diagram
└── src/
    ├── main/java/com/campus/
    │   ├── Main.java                       # Application bootstrap & dependency injection
    │   ├── exception/                      # Custom exception hierarchy (Unit 2)
    │   │   ├── BookingConflictException.java
    │   │   ├── DatabaseOperationException.java
    │   │   ├── InvalidInputException.java
    │   │   ├── InvalidLoginException.java
    │   │   └── ResourceNotFoundException.java
    │   ├── model/                          # OOP Domain models & Interfaces (Unit 1)
    │   │   ├── User.java                   # Abstract base user class
    │   │   ├── Student.java                # Concrete Student subclass
    │   │   ├── Admin.java                  # Concrete Admin subclass
    │   │   ├── Resource.java               # Campus resource entity
    │   │   ├── Booking.java                # Booking entity
    │   │   ├── ServiceRequest.java         # Maintenance ticket entity
    │   │   ├── AuditEntry.java             # System audit record
    │   │   ├── Reportable.java             # Functional reporting interface
    │   │   └── enums/                      # Typesafe domain enumerations
    │   │       ├── Role.java
    │   │       ├── ResourceType.java
    │   │       ├── ResourceStatus.java
    │   │       ├── BookingStatus.java
    │   │       ├── RequestStatus.java
    │   │       └── Priority.java
    │   ├── dao/                            # Data Access Objects (JDBC - Unit 5)
    │   │   ├── UserDAO.java
    │   │   ├── ResourceDAO.java
    │   │   ├── BookingDAO.java
    │   │   ├── ServiceRequestDAO.java
    │   │   └── AuditDAO.java
    │   ├── service/                        # Business Logic & Algorithms (Units 3 & 4)
    │   │   ├── AuthService.java            # Auth state & session management
    │   │   ├── ResourceService.java        # Resource catalog operations
    │   │   ├── BookingService.java         # Thread-safe mutex booking logic
    │   │   ├── ServiceRequestService.java  # Ticket lifecycle management
    │   │   ├── ReportService.java          # Java Streams analytics engine
    │   │   └── AuditService.java           # Vector-buffered audit trail
    │   ├── ui/                             # Presentation Layer (CLI)
    │   │   ├── ConsoleUI.java              # Main navigation & landing menu
    │   │   ├── StudentMenu.java            # Student portal
    │   │   └── AdminMenu.java              # Admin portal & concurrency demo
    │   └── util/                           # Utilities & Infrastructure (Units 2 & 5)
    │       ├── DatabaseManager.java        # Singleton SQLite connection manager
    │       ├── DatabaseInitializer.java    # Schema creation & demo seeder
    │       ├── InputValidator.java         # Centralized regex/range validation
    │       ├── PasswordUtil.java           # SHA-256 cryptographic hashing
    │       ├── FileUtil.java               # Safe file I/O operations
    │       └── Logger.java                 # Thread-safe application logging
    └── test/java/com/campus/               # JUnit 5 Test Suites
        ├── util/InputValidatorTest.java    # 23 input validation tests
        ├── dao/DatabaseTest.java           # Full CRUD integration tests
        ├── service/BookingServiceTest.java # Booking & multithreaded race tests
        └── service/ReportServiceTest.java  # Stream aggregations & file export tests
```

---

