package com.sunrisedental.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_ERROR", 401);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_ERROR", cause);
    }
}