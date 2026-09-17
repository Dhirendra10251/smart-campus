# Sequence Diagram: Thread-Safe Resource Booking Flow

The sequence diagram details the end-to-end execution path of a booking request, demonstrating input validation, resource status checks, fine-grained mutex lock acquisition, conflict detection, database insertion, and audit logging.

```mermaid
sequenceDiagram
    autonumber
    actor Student as Student (User)
    participant UI as StudentMenu (CLI)
    participant Validator as InputValidator
    participant BService as BookingService
    participant RService as ResourceService
    participant LockMap as ConcurrentHashMap (Mutex)
    participant BDAO as BookingDAO
    participant DB as SQLite Database
    participant Audit as AuditService

    Student->>UI: Enter (Resource ID, Date, Start, End)
    UI->>Validator: validateDate(dateStr), validateTime(start, end)
    Validator-->>UI: Validated temporal objects
    
    UI->>BService: bookResource(userId, resId, date, start, end)
    
    BService->>RService: getResourceById(resId)
    RService-->>BService: Resource entity (Status: AVAILABLE)
    
    BService->>LockMap: computeIfAbsent(resId, () -> new Object())
    LockMap-->>BService: Mutex lock object for resId
    
    critical Acquire Mutex Lock on Resource
        BService->>BDAO: hasConflict(resId, date, start, end, 0)
        BDAO->>DB: SELECT COUNT(*) WHERE overlap check
        DB-->>BDAO: Count = 0 (No conflict)
        BDAO-->>BService: false
        
        BService->>BDAO: insert(booking)
        BDAO->>DB: INSERT INTO bookings VALUES (...)
        DB-->>BDAO: Generated booking_id = 42
        BDAO-->>BService: 42
        
        BService->>Audit: log(userId, "BOOKING_CREATE", details)
        Audit->>DB: INSERT INTO audit_logs VALUES (...)
    end
    
    BService-->>UI: Return confirmed Booking object
    UI-->>Student: Display: [SUCCESS] Booking Confirmed (ID: #42)
```
