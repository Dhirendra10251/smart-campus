# Database Architecture & Schema Specification

## 1. Overview

The persistence layer is implemented using an embedded **SQLite 3** relational database engine accessed via the standard **JDBC API (`org.xerial:sqlite-jdbc`)**. 

- **Database Path:** `data/campus.db`
- **Foreign Key Enforcement:** Explicitly enabled at runtime via `PRAGMA foreign_keys = ON;`.
- **Initialization Strategy:** Idempotent schema bootstrapping executed on application startup via `DatabaseInitializer.java`. Tables are created using `CREATE TABLE IF NOT EXISTS` clauses, and initial seed data is protected against duplicate insertion using `INSERT OR IGNORE` and existence checks.

---

## 2. Relational Schema & Tables

### 2.1 Table: `users`
Stores user credentials, contact information, and role authorization.

```sql
CREATE TABLE IF NOT EXISTS users (
    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    email         TEXT    UNIQUE NOT NULL,
    password_hash TEXT    NOT NULL,
    role          TEXT    NOT NULL DEFAULT 'STUDENT',
    created_at    TEXT    NOT NULL
);
```

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `user_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier for each registered account |
| `name` | TEXT | NOT NULL | User's full name |
| `email` | TEXT | UNIQUE, NOT NULL | Unique login email address |
| `password_hash` | TEXT | NOT NULL | Hex-encoded SHA-256 salted password hash |
| `role` | TEXT | NOT NULL, DEFAULT 'STUDENT' | Role identifier (`STUDENT` or `ADMIN`) |
| `created_at` | TEXT | NOT NULL | Registration ISO timestamp (`yyyy-MM-dd HH:mm:ss`) |

---

### 2.2 Table: `resources`
Represents bookable facilities, labs, halls, and hardware equipment.

```sql
CREATE TABLE IF NOT EXISTS resources (
    resource_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    type         TEXT    NOT NULL,
    location     TEXT    NOT NULL,
    capacity     INTEGER DEFAULT 0,
    status       TEXT    NOT NULL DEFAULT 'AVAILABLE',
    created_at   TEXT    NOT NULL
);
```

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `resource_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier for the campus asset |
| `name` | TEXT | NOT NULL | Human-readable name (e.g., `"Computer Lab A"`) |
| `type` | TEXT | NOT NULL | Type enum: `LAB`, `CLASSROOM`, `SEMINAR_HALL`, `SPORTS_FACILITY`, `EQUIPMENT` |
| `location` | TEXT | NOT NULL | Campus location or building block |
| `capacity` | INTEGER | DEFAULT 0 | Maximum seating or equipment quantity |
| `status` | TEXT | NOT NULL, DEFAULT 'AVAILABLE' | Status enum: `AVAILABLE`, `UNAVAILABLE`, `UNDER_MAINTENANCE` |
| `created_at` | TEXT | NOT NULL | Timestamp when asset was recorded |

---

### 2.3 Table: `bookings`
Records time-slot reservations made by users for specific resources.

```sql
CREATE TABLE IF NOT EXISTS bookings (
    booking_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL,
    resource_id  INTEGER NOT NULL,
    booking_date TEXT    NOT NULL,
    start_time   TEXT    NOT NULL,
    end_time     TEXT    NOT NULL,
    status       TEXT    NOT NULL DEFAULT 'CONFIRMED',
    created_at   TEXT    NOT NULL,
    FOREIGN KEY (user_id)     REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
);
```

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `booking_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique booking receipt ID |
| `user_id` | INTEGER | NOT NULL, FK ➔ `users(user_id)` | User who created the reservation |
| `resource_id` | INTEGER | NOT NULL, FK ➔ `resources(resource_id)` | Reserved campus facility |
| `booking_date`| TEXT | NOT NULL | Reservation date in `yyyy-MM-dd` format |
| `start_time` | TEXT | NOT NULL | Start time in 24-hour `HH:mm` format |
| `end_time` | TEXT | NOT NULL | End time in 24-hour `HH:mm` format |
| `status` | TEXT | NOT NULL, DEFAULT 'CONFIRMED' | Booking status: `CONFIRMED`, `CANCELLED`, `REJECTED` |
| `created_at` | TEXT | NOT NULL | Timestamp when booking was booked |

**Conflict Detection Logic:**
Two bookings $(S_1, E_1)$ and $(S_2, E_2)$ for the same `resource_id` on the same `booking_date` conflict if:
$$\max(S_1, S_2) < \min(E_1, E_2)$$
Translated to SQLite PreparedStatement SQL:
```sql
SELECT COUNT(*) FROM bookings
WHERE resource_id = ?
  AND booking_date = ?
  AND status = 'CONFIRMED'
  AND booking_id != ?
  AND start_time < ?
  AND end_time > ?;
```

---

### 2.4 Table: `service_requests`
Maintains maintenance tickets, repairs, and support requests.

```sql
CREATE TABLE IF NOT EXISTS service_requests (
    request_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL,
    resource_id  INTEGER,
    description  TEXT    NOT NULL,
    category     TEXT    NOT NULL DEFAULT 'GENERAL',
    priority     TEXT    NOT NULL DEFAULT 'MEDIUM',
    status       TEXT    NOT NULL DEFAULT 'OPEN',
    created_at   TEXT    NOT NULL,
    updated_at   TEXT    NOT NULL,
    FOREIGN KEY (user_id)     REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE SET NULL
);
```

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `request_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique maintenance ticket number |
| `user_id` | INTEGER | NOT NULL, FK ➔ `users(user_id)` | Student or staff who filed the request |
| `resource_id` | INTEGER | NULLABLE, FK ➔ `resources(resource_id)` | Associated equipment or facility (if applicable) |
| `description` | TEXT | NOT NULL | Issue narrative |
| `category` | TEXT | NOT NULL, DEFAULT 'GENERAL' | Category (e.g., `ELECTRICAL`, `HARDWARE`, `NETWORK`) |
| `priority` | TEXT | NOT NULL, DEFAULT 'MEDIUM' | Priority enum: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `status` | TEXT | NOT NULL, DEFAULT 'OPEN' | State: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `created_at` | TEXT | NOT NULL | Initial filing timestamp |
| `updated_at` | TEXT | NOT NULL | Last modification timestamp |

---

### 2.5 Table: `audit_logs`
Chronological, append-only security and operational audit trail.

```sql
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id   INTEGER,
    action    TEXT NOT NULL,
    details   TEXT,
    timestamp TEXT NOT NULL
);
```

---

## 3. Pre-Seeded Default Dataset

When `DatabaseInitializer.initialize()` executes for the first time, it seeds:
- **3 Users:** 1 Administrator (`admin@campus.edu`), 2 Students (`alice@campus.edu`, `bob@campus.edu`).
- **10 Resources:**
  1. `Computer Lab A` (Lab, 40 seats, Available)
  2. `Computer Lab B` (Lab, 40 seats, Available)
  3. `Seminar Hall 101` (Seminar Hall, 80 seats, Available)
  4. `Seminar Hall 202` (Seminar Hall, 60 seats, Available)
  5. `Classroom 301` (Classroom, 60 seats, Available)
  6. `Classroom 302` (Classroom, 60 seats, Available)
  7. `Basketball Court` (Sports Facility, 30 capacity, Available)
  8. `Badminton Court` (Sports Facility, 10 capacity, Available)
  9. `Projector Set 1` (Equipment, 1 capacity, Available)
  10. `Projector Set 2` (Equipment, 1 capacity, Under Maintenance)
- **4 Sample Bookings:** Validating confirmed and cancelled historical records.
- **3 Sample Service Requests:** Demonstrating `HIGH`, `MEDIUM`, and `LOW` tickets across different statuses (`OPEN`, `IN_PROGRESS`, `RESOLVED`).
