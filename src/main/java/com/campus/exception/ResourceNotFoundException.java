package com.campus.exception;

/**
 * Thrown when a requested resource (campus resource, user, booking) is not found.
 * Unit 3 — Custom Exception demonstration.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
