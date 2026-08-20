package com.sunrisedental.exception;

/**
 * Exception thrown for database-related errors.
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR", 500);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
    }
}