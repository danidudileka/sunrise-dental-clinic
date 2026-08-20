package com.sunrisedental.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.util.JsonUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Handles all exceptions and converts them to appropriate JSON responses.
 */
public class GlobalExceptionHandler {
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    /**
     * Handle exception and write appropriate response
     */
    public static void handleException(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Exception e) throws IOException {

        ApiResponse apiResponse;
        int statusCode;

        if (e instanceof ValidationException) {
            ValidationException ve = (ValidationException) e;
            statusCode = ve.getStatusCode();
            apiResponse = ApiResponse.error(ve.getMessage(), ve.getValidationErrors());

        } else if (e instanceof AuthenticationException) {
            AuthenticationException ae = (AuthenticationException) e;
            statusCode = ae.getStatusCode();
            apiResponse = ApiResponse.error(ae.getMessage());

        } else if (e instanceof NotFoundException) {
            NotFoundException nfe = (NotFoundException) e;
            statusCode = nfe.getStatusCode();
            apiResponse = ApiResponse.error(nfe.getMessage());

        } else if (e instanceof DatabaseException) {
            DatabaseException de = (DatabaseException) e;
            statusCode = de.getStatusCode();
            apiResponse = ApiResponse.error(de.getMessage());
            logger.error("Database error", e);

        } else if (e instanceof AppException) {
            AppException ae = (AppException) e;
            statusCode = ae.getStatusCode();
            apiResponse = ApiResponse.error(ae.getMessage());
            logger.error("Application error: " + ae.getMessage(), e);

        } else {
            // Unknown exception
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            apiResponse = ApiResponse.error("An unexpected error occurred");
            logger.error("Unexpected error", e);
        }

        // Set response status and content type
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Write error response
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JsonUtil.toJson(apiResponse));
            writer.flush();
        }
    }

    /**
     * Create error response for servlet exceptions
     */
    public static void handleServletException(HttpServletRequest request,
                                              HttpServletResponse response,
                                              int statusCode,
                                              String message) throws IOException {

        ApiResponse apiResponse;

        switch (statusCode) {
            case HttpServletResponse.SC_NOT_FOUND:
                apiResponse = ApiResponse.error("Resource not found: " + request.getRequestURI());
                break;

            case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
                apiResponse = ApiResponse.error("Method not allowed: " + request.getMethod());
                break;

            case HttpServletResponse.SC_UNAUTHORIZED:
                apiResponse = ApiResponse.error("Unauthorized access");
                break;

            case HttpServletResponse.SC_FORBIDDEN:
                apiResponse = ApiResponse.error("Access forbidden");
                break;

            default:
                apiResponse = ApiResponse.error(message != null ? message : "Request failed");
                break;
        }

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(JsonUtil.toJson(apiResponse));
            writer.flush();
        }
    }
}