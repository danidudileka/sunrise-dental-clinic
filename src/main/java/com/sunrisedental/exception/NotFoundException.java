package com.sunrisedental.exception;

/**
 * Exception thrown when a resource is not found.
 */
public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(message, "NOT_FOUND", 404);
    }

    public NotFoundException(String resource, String identifier) {
        super(resource + " not found with identifier: " + identifier, "NOT_FOUND", 404);
    }
}