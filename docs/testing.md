# Testing Strategy & Test Execution Matrix

## 1. Testing Philosophy & Framework

The testing suite is designed around **JUnit 5 (Jupiter)** to validate functional correctness, input boundaries, relational persistence, and multithreaded thread-safety under concurrent load.

- **Test Framework:** JUnit 5.10.2 (`junit-jupiter-api`, `junit-jupiter-engine`, `junit-jupiter-params`)
- **Surefire Version:** Maven Surefire Plugin 3.2.5
- **Test Isolation Strategy:** Integration tests utilize a temporary, isolated database file (`data/test_*.db`) that is dynamically created, initialized, and deleted for each test lifecycle (`@BeforeEach` / `@AfterEach`), ensuring zero contamination of production data (`data/campus.db`).

---

## 2. Test Execution Commands

### Run All Tests via Maven Wrapper
```powershell
# In Windows PowerShell:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
.\mvnw.cmd test
```

### Run a Specific Test Suite
```powershell
.\mvnw.cmd test -Dtest=BookingServiceTest
.\mvnw.cmd test -Dtest=InputValidatorTest
.\mvnw.cmd test -Dtest=DatabaseTest
.\mvnw.cmd test -Dtest=ReportServiceTest
```

---

## 3. Test Suites & Coverage Matrix

A total of **37 automated unit and integration tests** are implemented across four dedicated test classes:

| Test Class | Test Target | Count | Key Test Scenarios |
| :--- | :--- | :---: | :--- |
| [`InputValidatorTest`](../src/test/java/com/campus/util/InputValidatorTest.java) | `InputValidator.java` | 23 | • Valid RFC-compliant email matching<br>• Malformed email rejection<br>• Whitespace and null non-empty validation<br>• Password minimum length verification<br>• Positive and non-negative integer bounds<br>• Numeric menu choice interval checks<br>• ISO date parsing & rejection of past dates<br>• Time format parsing & range validation (`start < end`)<br>• Case-insensitive enum reflection parsing |
| [`DatabaseTest`](../src/test/java/com/campus/dao/DatabaseTest.java) | All DAOs & Schema | 5 | • User insert, lookup by email, update role, delete<br>• Resource insert, status update, filter by type, delete<br>• Booking interval collision query verification<br>• Service ticket priority and status updates with timestamps<br>• Audit trail log insertion and retrieval |
| [`BookingServiceTest`](../src/test/java/com/campus/service/BookingServiceTest.java) | `BookingService.java` | 5 | • Successful reservation creation<br>• Collision rejection on overlapping intervals<br>• Collision rejection on identical & enclosing intervals<br>• Guard against booking resources in maintenance<br>• **Multithreaded Race Test (10 concurrent threads simultaneously requesting identical slot — exactly 1 succeeds, 9 fail with `BookingConflictException`)** |
| [`ReportServiceTest`](../src/test/java/com/campus/service/ReportServiceTest.java) | `ReportService.java` | 4 | • Resource inventory aggregation via Streams<br>• Booking statistics & most-booked resource discovery<br>• Service ticket breakdown by priority and resolution state<br>• Physical report file export verification to `data/reports/` |
| **Total** | | **37** | **100% Passed (0 Failures, 0 Errors, 0 Skipped)** |

---

## 4. Deep-Dive: Multithreaded Concurrency Test Case

The concurrency stress test in `BookingServiceTest.java` validates that our per-resource lock correctly eliminates race conditions:

```java
@Test
@DisplayName("Multithreaded booking concurrency test (thread-safe synchronization)")
void testConcurrentBookings() throws InterruptedException {
    int threadCount = 10;
    String bookingDate = LocalDate.now().plusDays(10).toString();
    String startTime = "15:00";
    String endTime = "17:00";
    int resourceId = 3; // Seminar Hall 101

    CountDownLatch readyLatch = new CountDownLatch(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger conflictCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
        new Thread(() -> {
            readyLatch.countDown();
            try {
                startLatch.await(); // Hold all threads at starting gate
                bookingService.bookResource(2, resourceId, bookingDate, startTime, endTime);
                successCount.incrementAndGet();
            } catch (BookingConflictException e) {
                conflictCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        }).start();
    }

    readyLatch.await();
    startLatch.countDown(); // Release all 10 threads simultaneously
    doneLatch.await();

    assertEquals(1, successCount.get(), "Exactly one thread must succeed");
    assertEquals(threadCount - 1, conflictCount.get(), "9 threads must get BookingConflictException");
}
```

### Verification Result
```
[INFO] Running com.campus.service.BookingServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.445 s
```
All 10 threads hit the service at the exact same microsecond; 1 thread successfully claims the reservation, and 9 threads receive the expected `BookingConflictException` without data corruption.
