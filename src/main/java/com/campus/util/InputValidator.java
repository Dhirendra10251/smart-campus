package com.campus.util;

import com.campus.exception.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * InputValidator — Static utility methods for all CLI input validation.
 *
 * All methods throw InvalidInputException with a user-friendly message on failure,
 * so callers can catch and display the message without showing stack traces.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // -------------------------------------------------------------------------
    // String validators
    // -------------------------------------------------------------------------

    /**
     * Validates that a string is not null or blank.
     * Returns the trimmed string on success.
     */
    public static String validateNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    /**
     * Validates email format using a regex pattern.
     */
    public static String validateEmail(String email) {
        email = validateNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Invalid email format: " + email);
        }
        return email.toLowerCase();
    }

    /**
     * Validates a password — must be at least 6 characters.
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters.");
        }
    }

    // -------------------------------------------------------------------------
    // Numeric validators
    // -------------------------------------------------------------------------

    /**
     * Parses an integer from a string and validates it is >= 1.
     */
    public static int validatePositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 1) {
                throw new InvalidInputException(fieldName + " must be a positive number (>= 1).");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a valid integer. Got: '" + value + "'");
        }
    }

    /**
     * Parses an integer from a string and validates it is >= 0.
     */
    public static int validateNonNegativeInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new InvalidInputException(fieldName + " cannot be negative.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a valid integer. Got: '" + value + "'");
        }
    }

    /**
     * Parses menu choice integer — must be in range [min, max] inclusive.
     */
    public static int validateMenuChoice(String value, int min, int max) {
        try {
            int choice = Integer.parseInt(value.trim());
            if (choice < min || choice > max) {
                throw new InvalidInputException(
                    "Choice must be between " + min + " and " + max + ".");
            }
            return choice;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a number between " + min + " and " + max + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Date / Time validators
    // -------------------------------------------------------------------------

    /**
     * Validates date string in yyyy-MM-dd format.
     * Also rejects dates in the past.
     */
    public static LocalDate validateDate(String dateStr) {
        try {
            dateStr = validateNonEmpty(dateStr, "Date");
            LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
            if (date.isBefore(LocalDate.now())) {
                throw new InvalidInputException("Booking date cannot be in the past. Got: " + dateStr);
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date format. Use yyyy-MM-dd. Got: " + dateStr);
        }
    }

    /**
     * Validates time string in HH:mm format.
     */
    public static LocalTime validateTime(String timeStr, String fieldName) {
        try {
            timeStr = validateNonEmpty(timeStr, fieldName);
            return LocalTime.parse(timeStr, TIME_FMT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid time format for " + fieldName + ". Use HH:mm. Got: " + timeStr);
        }
    }

    /**
     * Validates that start time is strictly before end time.
     */
    public static void validateTimeRange(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new InvalidInputException("End time must be after start time.");
        }
    }

    // -------------------------------------------------------------------------
    // Enum validators
    // -------------------------------------------------------------------------

    /**
     * Validates an enum value by name (case-insensitive).
     * Returns the canonical enum name string.
     */
    public static <T extends Enum<T>> T validateEnum(String value, Class<T> enumClass, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            StringBuilder valid = new StringBuilder();
            for (T constant : enumClass.getEnumConstants()) {
                valid.append(constant.name()).append(", ");
            }
            throw new InvalidInputException(
                "Invalid " + fieldName + ": '" + value + "'. Valid values: " +
                valid.toString().replaceAll(", $", ""));
        }
    }
}
