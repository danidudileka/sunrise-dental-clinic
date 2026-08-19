package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a login attempt for security tracking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {
    private int attemptId;
    private String username;
    private LocalDateTime attemptTime;
    private String ipAddress;
    private boolean success;
}