package com.campus.util;

import com.campus.exception.InvalidInputException;
import com.campus.model.enums.Priority;
import com.campus.model.enums.ResourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputValidator Tests")
class InputValidatorTest {

    @Test
    @DisplayName("Valid non-empty string returns trimmed result")
    void testValidateNonEmpty_Success() {
        String result = InputValidator.validateNonEmpty("   Engineering Lab   ", "Lab Name");
        assertEquals("Engineering Lab", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("Empty or whitespace string throws InvalidInputException")
    void testValidateNonEmpty_Failure(String input) {
        assertThrows(InvalidInputException.class,
            () -> InputValidator.validateNonEmpty(input, "Field"));
    }

    @Test
    @DisplayName("Null string throws InvalidInputException")
    void testValidateNonEmpty_Null() {
        assertThrows(InvalidInputException.class,
            () -> InputValidator.validateNonEmpty(null, "Field"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice@campus.edu", "student.123@university.ac.in", "admin@sub.domain.org"})
    @DisplayName("Valid emails pass validation")
    void testValidateEmail_Success(String email) {
        String validated = InputValidator.validateEmail(email);
        assertEquals(email.toLowerCase(), validated);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "test@", "@domain.com", "plainaddress", "test@domain"})
    @DisplayName("Malformed emails throw InvalidInputException")
    void testValidateEmail_Invalid(String email) {
        assertThrows(InvalidInputException.class,
            () -> InputValidator.validateEmail(email));
    }

    @Test
    @DisplayName("Password with >= 6 characters passes")
    void testValidatePassword_Valid() {
        assertDoesNotThrow(() -> InputValidator.validatePassword("pass123"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "a", ""})
    @DisplayName("Password with < 6 characters throws InvalidInputException")
    void testValidatePassword_TooShort(String pass) {
        assertThrows(InvalidInputException.class,
            () -> InputValidator.validatePassword(pass));
    }

    @Test
    @DisplayName("Positive integer parsing")
    void testValidatePositiveInt() {
        assertEquals(5, InputValidator.validatePositiveInt("5", "Capacity"));
        assertThrows(InvalidInputException.class, () -> InputValidator.validatePositiveInt("0", "Capacity"));
        assertThrows(InvalidInputException.class, () -> InputValidator.validatePositiveInt("-3", "Capacity"));
        assertThrows(InvalidInputException.class, () -> InputValidator.validatePositiveInt("abc", "Capacity"));
    }

    @Test
    @DisplayName("Non-negative integer parsing")
    void testValidateNonNegativeInt() {
        assertEquals(0, InputValidator.validateNonNegativeInt("0", "Count"));
        assertEquals(10, InputValidator.validateNonNegativeInt("10", "Count"));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateNonNegativeInt("-1", "Count"));
    }

    @Test
    @DisplayName("Menu choice within range")
    void testValidateMenuChoice() {
        assertEquals(2, InputValidator.validateMenuChoice("2", 1, 5));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateMenuChoice("0", 1, 5));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateMenuChoice("6", 1, 5));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateMenuChoice("xyz", 1, 5));
    }

    @Test
    @DisplayName("Date validation for today or future")
    void testValidateDate() {
        String todayStr = LocalDate.now().toString();
        LocalDate parsed = InputValidator.validateDate(todayStr);
        assertEquals(LocalDate.now(), parsed);

        String pastDate = LocalDate.now().minusDays(1).toString();
        assertThrows(InvalidInputException.class, () -> InputValidator.validateDate(pastDate));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateDate("not-a-date"));
    }

    @Test
    @DisplayName("Time validation and time range check")
    void testValidateTimeAndTimeRange() {
        LocalTime start = InputValidator.validateTime("10:00", "Start time");
        LocalTime end = InputValidator.validateTime("12:00", "End time");
        assertEquals(LocalTime.of(10, 0), start);
        assertEquals(LocalTime.of(12, 0), end);

        assertDoesNotThrow(() -> InputValidator.validateTimeRange(start, end));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateTimeRange(end, start));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateTimeRange(start, start));
        assertThrows(InvalidInputException.class, () -> InputValidator.validateTime("25:00", "Time"));
    }

    @Test
    @DisplayName("Enum parsing case-insensitive")
    void testValidateEnum() {
        ResourceType type = InputValidator.validateEnum("lab", ResourceType.class, "Resource Type");
        assertEquals(ResourceType.LAB, type);

        Priority priority = InputValidator.validateEnum("CRITICAL", Priority.class, "Priority");
        assertEquals(Priority.CRITICAL, priority);

        assertThrows(InvalidInputException.class,
            () -> InputValidator.validateEnum("UNKNOWN_TYPE", ResourceType.class, "Resource Type"));
    }
}
