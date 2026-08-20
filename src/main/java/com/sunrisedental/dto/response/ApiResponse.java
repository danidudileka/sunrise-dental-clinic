package com.sunrisedental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic API response wrapper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String status;
    private String message;
    private Object data;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    /**
     * Create success response
     */
    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response
     */
    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .status("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with validation errors
     */
    public static ApiResponse error(String message, Map<String, String> errors) {
        return ApiResponse.builder()
                .status("ERROR")
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create warning response
     */
    public static ApiResponse warning(String message, Object data) {
        return ApiResponse.builder()
                .status("WARNING")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}