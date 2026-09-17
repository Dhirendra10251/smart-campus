package com.campus.exception;

/**
 * Thrown when a booking request conflicts with an existing confirmed booking.
 * Unit 3 — Custom Exception; also used as signal in concurrency demonstration.
 */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
