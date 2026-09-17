# Architectural & Technical Design Decisions

This document outlines the rationale, trade-offs, and design justifications behind key architectural decisions in the **Smart Campus Resource & Service Management System**.

---

## 1. Concurrency: Per-Resource Mutex vs. Global Synchronized Method

### Context
In a booking system, preventing race conditions (two threads simultaneously reading "no conflict" and both inserting reservations for the same time slot) is critical.

### Decision
Implement fine-grained per-resource mutex locks using a `ConcurrentHashMap<Integer, Object>` rather than synchronizing the entire `bookResource()` method.

```java
Object lock = resourceLocks.computeIfAbsent(resourceId, k -> new Object());
synchronized (lock) {
    if (bookingDAO.hasConflict(resourceId, dateStr, startStr, endStr, 0)) {
        throw new BookingConflictException(...);
    }
    bookingDAO.insert(booking);
}
```

### Trade-Off Analysis
| Approach | Pros | Cons |
| :--- | :--- | :--- |
| **Synchronized Method (`public synchronized Booking bookResource(...)`)** | Extremely simple to implement. | Severe bottleneck. A thread booking "Basketball Court" unnecessarily blocks an unrelated thread booking "Computer Lab A". Total system throughput collapses under load. |
| **Per-Resource Mutex (`ConcurrentHashMap<Integer, Object>`) [CHOSEN]** | High concurrency throughput. Threads targeting *different* resources execute in parallel without contention, while requests for the *same* resource are strictly serialized. | Requires careful lock object management (`computeIfAbsent`) to avoid memory leaks. |

---

## 2. Persistence: Embedded SQLite vs. Client-Server RDBMS (MySQL/PostgreSQL)

### Context
The application requires persistent storage of users, facilities, reservations, tickets, and logs.

### Decision
Use **SQLite 3** via `sqlite-jdbc`.

### Rationale & Academic Defense
1. **Zero-Configuration Portability:** External RDBMS (e.g. MySQL) requires running a background service daemon, setting up user credentials, configuring TCP ports, and network firewalls. SQLite is completely self-contained within `data/campus.db`, guaranteeing that an evaluator or professor can run the project on any machine immediately with `./mvnw.cmd exec:java`.
2. **Standard SQL & JDBC Compatibility:** SQLite supports standard ANSI SQL syntax, primary keys, foreign keys (`PRAGMA foreign_keys = ON`), transactions, and standard JDBC `PreparedStatement` APIs.
3. **Appropriate Scale:** For an educational project and campus CLI application, SQLite's single-file ACID storage is optimal.

---

## 3. Database Connection: Singleton Connection vs. Connection Pool (HikariCP)

### Context
Managing database connections in a CLI application.

### Decision
Implement a synchronized **Singleton `DatabaseManager`** maintaining a single open `Connection`.

### Rationale
SQLite operates on a file-based storage architecture. In SQLite, having multiple concurrent connection handles attempting to write to the same file can trigger `database is locked` (`SQLITE_BUSY`) errors unless write-ahead logging (WAL) is heavily configured. A single shared connection managed by a Singleton eliminates connection thrashing and file-lock contention while being fully sufficient for a CLI workstation tool.

---

## 4. Collection Choices: Purpose-Driven Data Structures

Rather than using `ArrayList` exclusively, four distinct collections from the Java Collections Framework were selected based on algorithmic suitability:

| Collection | Location | Architectural Rationale |
| :--- | :--- | :--- |
| **`ArrayList`** | All DAOs & Services | Optimal for sequential iteration, index-based access, and sorting database query result sets. |
| **`HashMap`** | `AuthService.java`<br>`ReportService.java` | Delivers $O(1)$ constant-time lookup for in-memory session caching and key-value report grouping. |
| **`Vector`** | `AuditService.java` | Thread-safe synchronized dynamic array. Purposefully chosen for the live in-memory audit log stream where multiple threads simultaneously emit audit events. |
| **`Stack`** | `AdminMenu.java` | Follows Last-In-First-Out (LIFO) semantics to maintain an administrator action trail. Enables reviewing recent operations in reverse-chronological order (undo stack pattern). |

---

## 5. Reporting Engine: Java Streams API vs. Imperative For-Loops

### Context
Generating multi-dimensional summaries (e.g., booking counts grouped by resource, requests grouped by priority, finding top-booked resources).

### Decision
Adopt functional programming with the **Java Streams API** (`filter`, `map`, `groupingBy`, `counting`, `sorted`, `Collectors.joining`).

### Rationale
- **Declarative Readability:** Expresses *what* aggregation to calculate rather than managing mutable loop counters and nested accumulator maps.
- **Academic Rigor:** Directly addresses Unit 4 course requirements for modern Java functional pipelines and lambda expressions.
- **Maintainability:** Adding a new filter condition (e.g. filtering out cancelled bookings before grouping) requires only a single `.filter()` pipeline step.

---

## 6. Password Security: SHA-256 with Salt vs. Plaintext

### Context
Storing user credentials securely in the database.

### Decision
Implement `PasswordUtil.java` using `MessageDigest.getInstance("SHA-256")` combined with application-level salting.

### Rationale
Plaintext password storage is a severe vulnerability. Applying SHA-256 hashing protects student and administrator credentials in the database file while adhering strictly to standard Java cryptography APIs without pulling in heavy external dependencies (such as BCrypt or Spring Security).
