package com.sunrisedental.exception;

import lombok.Getter;

/**
 * Base exception class for application-specific exceptions.
 */
@Getter
public class AppException extends RuntimeException {
    private final String errorCode;
    private final int statusCode;

    public AppException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
        this.statusCode = 500;
    }

    public AppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 500;
    }

    public AppException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "APP_ERROR";
        this.statusCode = 500;
    }

    public AppException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = 500;
    }
}