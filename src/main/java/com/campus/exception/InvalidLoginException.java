package com.campus.exception;

/**
 * Thrown when authentication fails (wrong credentials or account not found).
 * Unit 3 — Custom Exception demonstration.
 */
public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
