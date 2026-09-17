package com.campus.exception;

/**
 * Wraps JDBC/SQL exceptions to decouple callers from java.sql internals.
 * Unit 3 — Custom Exception demonstration.
 */
public class DatabaseOperationException extends RuntimeException {
    public DatabaseOperationException(String message) {
        super(message);
    }
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
