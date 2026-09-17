# Academic Problem Statement & Course Evaluation Document

**Course:** Advanced Object-Oriented Programming in Java (VITyarthi Flipped Course Evaluation)  
**Project Title:** Smart Campus Resource & Service Management System  
**Format:** Production-Ready Command-Line Java Application  
**Student Submission Reference:** Individual Project Submission  

---

## 1. Problem Definition & Context

Collegiate institutions operate dozens of shared facilities and educational assets across multiple departments—ranging from computer labs, conference rooms, and smart lecture theaters to specialized sports courts and multimedia projection hardware. 

In traditional university environments, resource allocation and incident resolution suffer from critical systemic deficiencies:
1. **Unsynchronized Booking Channels:** When multiple student organizations or faculty representatives request the same room or lab at overlapping hours, manual logging or naive software systems without concurrency controls permit double-bookings.
2. **Disconnected Service & Maintenance Reporting:** Faulty equipment (e.g., burned-out projectors, broken air conditioning units, or damaged networking cables) is often reported verbally or via ad-hoc emails, resulting in untracked tickets, lingering downtime, and duplicate repair requests.
3. **Lack of Automated Telemetry & Auditability:** Administrators lack automated aggregation tools to discover facility utilization trends, peak reservation hours, or pending maintenance bottlenecks. Furthermore, security and governance mandate an immutable audit trail of who made, approved, or cancelled bookings.

The **Smart Campus Resource & Service Management System** resolves these challenges through a centralized, high-performance, terminal-based software application engineered with object-oriented best practices, concurrent data integrity, relational persistence, and automated analytics.

---

## 2. Project Objectives

The core objectives of this project are:
1. **Secure Role-Based Access Control (RBAC):** Establish distinct, cryptographically protected workspaces for Students (view facilities, book slots, raise tickets, monitor personal history) and Campus Administrators (manage catalog, adjudicate bookings, resolve tickets, view analytics, inspect audit trails).
2. **Deterministic Concurrency Control:** Prevent concurrent double-booking vulnerabilities using thread-safe fine-grained mutex synchronization on individual campus resources.
3. **Comprehensive Lifecycle Tracking:** Implement complete state machines for resource status (`AVAILABLE`, `UNAVAILABLE`, `UNDER_MAINTENANCE`), booking status (`CONFIRMED`, `CANCELLED`, `REJECTED`), and service requests (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`).
4. **Data Analytics via Java Streams:** Provide instantaneous, in-memory analytical reporting that groups, aggregates, and ranks campus activities.
5. **Robust Exception-Resilient UX:** Deliver an intuitive command-line interface that catches domain-specific edge cases gracefully, providing helpful feedback rather than abrupt program crashes.
6. **Fulfill University Syllabus Criteria:** Rigorously demonstrate practical proficiency across all 5 syllabus units of the Java course.

---

## 3. Scope of the System

### In-Scope Functional Modules
- **Authentication & Security:** User registration, SHA-256 salted password hashing, credential verification, and session state tracking.
- **Resource Management:** Listing, filtering by type/status, adding new resources, modifying status (e.g., placing equipment under maintenance), and resource decommissioning.
- **Booking Engine:** Interval-based reservation system with collision detection (rejecting identical, overlapping, or enclosing intervals), status transitions, and user cancellation rights.
- **Concurrency Demonstration:** Standalone, interactive multi-threaded race simulation validating that fine-grained mutex locks guarantee single-reservation correctness.
- **Service Request Management:** Structured trouble-ticketing linking complaints to campus resources with categorized urgency levels (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- **Reporting & File Export Engine:** On-demand generation of formatted ASCII reports with disk-export capabilities into `data/reports/`.
- **System Audit Trail:** Real-time event recording into both a persistent database table and an in-memory thread-safe `Vector` log buffer.
- **Undo / Administrative History:** Pushing administrative actions onto a `Stack` data structure for operational inspection.

### Architectural Boundaries & Limitations
- **Terminal Presentation:** The user interface is intentionally terminal-first (CLI) to eliminate unnecessary web/GUI framework overhead while highlighting pure Java language constructs, multithreading, and JDBC.
- **Database Engine:** Uses SQLite 3 embedded relational database for lightweight, self-contained portability without requiring external server setup (e.g., MySQL or PostgreSQL daemons).
- **Single-Host Concurrency:** Thread synchronization leverages JVM-level primitives (`synchronized` mutex and `ConcurrentHashMap`) suited for local process execution.

---

## 4. Target User Personas

| User Persona | Key Needs & Goals | Core Actions |
| :--- | :--- | :--- |
| **Student** | Needs hassle-free booking of labs and halls for club events or study sessions; quick maintenance reporting. | Browse catalog, book slots, view personal bookings, cancel reservations, submit service requests, check personal summary. |
| **Campus Administrator** | Needs centralized governance over facility availability, maintenance schedules, and utilization telemetry. | Add/edit resources, update booking statuses, reassign ticket priorities, trigger concurrency stress tests, export analytical reports, inspect audit trail. |

---

## 5. Evaluation Deliverables Checklist

- [x] Executable Terminal Java Application runnable via Maven (`.\mvnw.cmd exec:java`)
- [x] Complete Relational Database Schema with Automatic Table Generation and Demo Seeding
- [x] Automated Test Suite consisting of 37 JUnit 5 test cases passing with 100% success rate
- [x] Complete Architectural and Technical Documentation Suite (`docs/`)
- [x] Visual Diagrams (Mermaid-based Use Case, Class, Sequence, Workflow, and ER diagrams)
- [x] Formal Syllabus Mapping detailing exact compliance with Course Units 1 through 5
