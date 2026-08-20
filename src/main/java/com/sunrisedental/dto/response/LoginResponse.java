package com.sunrisedental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private int userId;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String sessionId;
    private boolean firstLogin;
    private String redirectUrl;
}