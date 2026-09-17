# Comprehensive Syllabus Mapping Document

**Course:** Advanced Object-Oriented Programming in Java  
**Target:** VITyarthi Flipped Course Evaluation (Units 1 to 5)

This document provides a precise, code-level mapping between the university Java syllabus concepts and their concrete implementation inside the **Smart Campus Resource & Service Management System**.

---

## Unit 1: Java Basics & Object-Oriented Programming

| Syllabus Concept | Implementation Location | Technical Explanation |
| :--- | :--- | :--- |
| **Classes & Objects** | [`com.campus.model.Resource`](../src/main/java/com/campus/model/Resource.java)<br>[`com.campus.model.Booking`](../src/main/java/com/campus/model/Booking.java) | Real-world entities modeled as stateful classes with private attributes, explicit constructors, getters, and business methods. |
| **Abstract Classes** | [`com.campus.model.User`](../src/main/java/com/campus/model/User.java) | `public abstract class User` cannot be directly instantiated. Defines common properties (`userId`, `name`, `email`, `role`) and an abstract method `public abstract String getDashboardTitle();`. |
| **Inheritance** | [`com.campus.model.Student`](../src/main/java/com/campus/model/Student.java)<br>[`com.campus.model.Admin`](../src/main/java/com/campus/model/Admin.java) | `Student extends User` and `Admin extends User`. Reuses base constructor via `super(...)` and inherits identity behaviors. |
| **Polymorphism & Method Overriding** | [`Student.java#L23`](../src/main/java/com/campus/model/Student.java)<br>[`Admin.java#L23`](../src/main/java/com/campus/model/Admin.java) | Both classes override `getDashboardTitle()`. In [`ConsoleUI.java`](../src/main/java/com/campus/ui/ConsoleUI.java), calling `user.getDashboardTitle()` dynamically dispatches to the subclass implementation at runtime. |
| **Interfaces** | [`com.campus.model.Reportable`](../src/main/java/com/campus/model/Reportable.java) | `public interface Reportable` declares the contract method `String toReportLine();`. Implemented by `Resource`, `Booking`, and `ServiceRequest`. |
| **Encapsulation** | All POJOs in `com.campus.model.*` | All instance state fields are declared `private`; access is restricted through public getters and validated setters. |
| **Constructors & `this` / `super`** | `User.java`, `Student.java`, `Admin.java` | Explicit parameterised constructors, constructor chaining via `this(...)`, and parent initialization via `super(...)`. |

---

## Unit 2: Packages, Exception Handling & Strings

| Syllabus Concept | Implementation Location | Technical Explanation |
| :--- | :--- | :--- |
| **Modular Packages** | `com.campus.*` | Strict package organization separating concerns: `.model`, `.model.enums`, `.dao`, `.service`, `.ui`, `.util`, `.exception`. |
| **Custom Checked / Unchecked Exceptions** | [`com.campus.exception.*`](../src/main/java/com/campus/exception/) | Custom domain exceptions:<br>• `BookingConflictException`: Slot collision<br>• `ResourceNotFoundException`: Non-existent entity<br>• `InvalidInputException`: Validation failure<br>• `InvalidLoginException`: Authentication failure<br>• `DatabaseOperationException`: JDBC/SQL error wrapper |
| **Exception Propagation (`throw` / `throws`)** | [`BookingService.java#L95`](../src/main/java/com/campus/service/BookingService.java) | Methods explicitly throw custom exceptions when domain rules are violated. |
| **Multi-Catch & Specific Catch Ordering** | [`AdminMenu.java#L308`](../src/main/java/com/campus/ui/AdminMenu.java) | Catches specific `NumberFormatException` first, followed by broader `IllegalArgumentException`, `ResourceNotFoundException`, ensuring no shadow catch blocks. |
| **Try-with-Resources & Finally** | [`Main.java#L60`](../src/main/java/com/campus/Main.java)<br>All DAOs in `com.campus.dao.*` | Automatic cleanup of `PreparedStatement`, `ResultSet`, and database connection via `finally` blocks and ARM (`AutoCloseable`). |
| **String Manipulation & RegEx** | [`InputValidator.java`](../src/main/java/com/campus/util/InputValidator.java) | Regex pattern matching for RFC-compliant emails (`EMAIL_PATTERN`), trimming, formatting dates/times, and `StringBuilder` string construction in `ReportService.java`. |
| **Wrapper Classes & Parsing** | `InputValidator.java`<br>`AdminMenu.java` | Conversions using `Integer.parseInt()`, `Boolean.parseBoolean()`, auto-boxing and unboxing with `Integer` and `Long`. |

---

## Unit 3: Multithreading & Concurrency

| Syllabus Concept | Implementation Location | Technical Explanation |
| :--- | :--- | :--- |
| **Thread Creation & Lifecycle** | [`AdminMenu.java#L330-L370`](../src/main/java/com/campus/ui/AdminMenu.java)<br>[`BookingServiceTest.java#L110`](../src/test/java/com/campus/service/BookingServiceTest.java) | Threads created using `new Thread(Runnable)` and lambdas; lifecycle managed using `start()`, `join()`, and synchronization barriers. |
| **Thread Synchronization & Critical Section** | [`BookingService.java#L114-L138`](../src/main/java/com/campus/service/BookingService.java) | `synchronized (lock) { ... }` block guarantees mutual exclusion across the check-then-insert critical section. |
| **Fine-Grained Mutex Locking** | [`BookingService.java#L67`](../src/main/java/com/campus/service/BookingService.java) | Instead of synchronizing the entire method (coarse-grained bottleneck), a `ConcurrentHashMap<Integer, Object> resourceLocks` maintains mutexes per resource ID. Two threads booking *different* resources execute concurrently without blocking. |
| **Concurrency Coordination Primitives** | [`BookingServiceTest.java#L118-L144`](../src/test/java/com/campus/service/BookingServiceTest.java) | Uses `CountDownLatch` (ready latch, start latch, done latch) to simulate sudden burst traffic, testing deterministic race prevention. |
| **Thread-Safe Shared Collections** | `ConcurrentHashMap` in `BookingService.java`<br>`Vector<AuditEntry>` in `AuditService.java` | Thread-safe data structures preventing memory corruption and `ConcurrentModificationException`. |

---

## Unit 4: Collections Framework, Generics & Streams API

| Syllabus Concept | Implementation Location | Technical Explanation |
| :--- | :--- | :--- |
| **`ArrayList`** | All DAOs and Services | Dynamic resizing list used for returning ordered sequences of records (e.g. `List<Resource> list = new ArrayList<>();`). |
| **`HashMap`** | [`AuthService.java`](../src/main/java/com/campus/service/AuthService.java)<br>[`ReportService.java`](../src/main/java/com/campus/service/ReportService.java) | Used for O(1) key-value session caching and in-memory aggregation grouping. |
| **`Vector`** | [`AuditService.java#L23`](../src/main/java/com/campus/service/AuditService.java) | Thread-safe legacy collection deliberately employed as an in-memory live audit buffer. Justified by multi-threaded concurrent event logging. |
| **`Stack`** | [`AdminMenu.java#L38`](../src/main/java/com/campus/ui/AdminMenu.java) | LIFO data structure storing administrative action history. Used for tracking recent operations with `push()` and inspection with `peek()`. |
| **Generics** | [`InputValidator.java#L160`](../src/main/java/com/campus/util/InputValidator.java)<br>`Optional<T>` in DAOs | Generic method `<T extends Enum<T>> T validateEnum(...)` guarantees compile-time type safety across any Enum type. DAOs return `Optional<T>` to eliminate null pointers. |
| **Streams API: `filter()`** | [`ReportService.java#L108`](../src/main/java/com/campus/service/ReportService.java) | Selects only active confirmed bookings or unresolved requests (`status == OPEN || IN_PROGRESS`). |
| **Streams API: `map()`** | [`ReportService.java#L87`](../src/main/java/com/campus/service/ReportService.java) | Transforms domain entities into formatted report lines via `Resource::toReportLine`. |
| **Streams API: `collect()` & `groupingBy()`** | [`ReportService.java#L64-L70`](../src/main/java/com/campus/service/ReportService.java) | Groups items by Category, Status, or Priority: `Collectors.groupingBy(Resource::getType, Collectors.counting())`. |
| **Streams API: `sorted()` & Comparator** | [`ReportService.java#L114-L117`](../src/main/java/com/campus/service/ReportService.java) | Ranks facilities by utilization: `sorted(Map.Entry.<String, Long>comparingByValue().reversed())`. |

---

## Unit 5: JDBC & File I/O

| Syllabus Concept | Implementation Location | Technical Explanation |
| :--- | :--- | :--- |
| **JDBC Connection & Driver** | [`DatabaseManager.java#L77`](../src/main/java/com/campus/util/DatabaseManager.java) | Loads SQLite driver via `Class.forName("org.sqlite.JDBC")` and creates connection via `DriverManager.getConnection(url)`. |
| **`PreparedStatement` (SQL Injection Prevention)** | All DAOs in `com.campus.dao.*` | 100% parameterised queries (`?` placeholders) with typed bindings (`setString`, `setInt`, `setNull`). Zero string concatenation in SQL queries. |
| **`ResultSet` Iteration & Mapping** | All DAOs in `com.campus.dao.*` | Traverses cursor rows using `while (rs.next())` and maps database fields into domain model entities. |
| **Auto-Generated Keys** | [`UserDAO.java#L40`](../src/main/java/com/campus/dao/UserDAO.java) | Uses `Statement.RETURN_GENERATED_KEYS` to retrieve the database-generated primary key upon insertion. |
| **File I/O (Writing & Appending)** | [`FileUtil.java`](../src/main/java/com/campus/util/FileUtil.java) | Utilizes `BufferedWriter`, `FileWriter`, and `java.nio.file.Files` to write exported analytics reports and maintain runtime logs. |
| **Application Logging System** | [`Logger.java`](../src/main/java/com/campus/util/Logger.java) | Centralized, thread-safe file logger recording `[INFO]`, `[WARN]`, `[ERROR]` messages with timestamps into `data/logs/application.log`. |
