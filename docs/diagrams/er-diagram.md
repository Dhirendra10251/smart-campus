# Entity-Relationship (ER) Diagram

The following Entity-Relationship diagram outlines the logical data models, primary keys, foreign key constraints, and cardinalities implemented in the SQLite database schema.

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : "places"
    USERS ||--o{ SERVICE_REQUESTS : "raises"
    USERS ||--o{ AUDIT_LOGS : "triggers"
    RESOURCES ||--o{ BOOKINGS : "is reserved in"
    RESOURCES ||--o{ SERVICE_REQUESTS : "is subject of"

    USERS {
        INTEGER user_id PK "Auto Increment"
        TEXT name "Full Name"
        TEXT email "Unique, Not Null"
        TEXT password_hash "SHA-256 Hash"
        TEXT role "STUDENT or ADMIN"
        TEXT created_at "ISO Timestamp"
    }

    RESOURCES {
        INTEGER resource_id PK "Auto Increment"
        TEXT name "Resource Title"
        TEXT type "LAB, CLASSROOM, SEMINAR_HALL, etc."
        TEXT location "Campus Block / Room"
        INTEGER capacity "Max Capacity"
        TEXT status "AVAILABLE, UNAVAILABLE, etc."
        TEXT created_at "ISO Timestamp"
    }

    BOOKINGS {
        INTEGER booking_id PK "Auto Increment"
        INTEGER user_id FK "References USERS(user_id)"
        INTEGER resource_id FK "References RESOURCES(resource_id)"
        TEXT booking_date "yyyy-MM-dd"
        TEXT start_time "HH:mm"
        TEXT end_time "HH:mm"
        TEXT status "CONFIRMED, CANCELLED, REJECTED"
        TEXT created_at "ISO Timestamp"
    }

    SERVICE_REQUESTS {
        INTEGER request_id PK "Auto Increment"
        INTEGER user_id FK "References USERS(user_id)"
        INTEGER resource_id FK "Nullable FK to RESOURCES"
        TEXT description "Issue Narrative"
        TEXT category "GENERAL, ELECTRICAL, etc."
        TEXT priority "LOW, MEDIUM, HIGH, CRITICAL"
        TEXT status "OPEN, IN_PROGRESS, RESOLVED, CLOSED"
        TEXT created_at "ISO Timestamp"
        TEXT updated_at "ISO Timestamp"
    }

    AUDIT_LOGS {
        INTEGER log_id PK "Auto Increment"
        INTEGER user_id "Nullable Actor ID"
        TEXT action "Operation Code"
        TEXT details "Descriptive Context"
        TEXT timestamp "ISO Timestamp"
    }
```
