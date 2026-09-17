package com.campus.exception;

/**
 * Thrown when user-supplied input fails validation.
 * Unit 3 — Custom Exception demonstration.
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
