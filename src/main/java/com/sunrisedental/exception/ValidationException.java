package com.sunrisedental.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
@Getter
public class ValidationException extends AppException {
    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, "VALIDATION_ERROR", 400);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String field, String errorMessage) {
        super(errorMessage, "VALIDATION_ERROR", 400);
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(field, errorMessage);
    }

    /**
     * Add a validation error
     */
    public void addValidationError(String field, String message) {
        this.validationErrors.put(field, message);
    }

    /**
     * Check if there are any validation errors
     */
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
}