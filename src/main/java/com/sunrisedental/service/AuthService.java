package com.sunrisedental.service;

import com.sunrisedental.dao.LoginAttemptDao;
import com.sunrisedental.dao.UserDao;
import com.sunrisedental.dto.request.LoginRequest;
import com.sunrisedental.dto.response.LoginResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.ValidationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for authentication operations.
 * Handles login logic and security checks.
 */
public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserDao userDao;
    private final LoginAttemptDao loginAttemptDao;

    // Configuration constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    public AuthService() {
        this.userDao = new UserDao();
        this.loginAttemptDao = new LoginAttemptDao();
    }

    /**
     * Authenticate user with username and password
     */
    public LoginResponse login(LoginRequest loginRequest, String ipAddress) {
        // Validate input
        validateLoginRequest(loginRequest);

        String username = loginRequest.getUsername().trim();
        String password = loginRequest.getPassword();

        // Check if account is locked
        if (isAccountLocked(username)) {
            loginAttemptDao.recordAttempt(username, ipAddress, false);
            throw new AuthenticationException("Account is temporarily locked. Please try again later.");
        }

        // Find user
        Optional<User> userOptional = userDao.findByUsername(username);

        if (userOptional.isEmpty()) {
            loginAttemptDao.recordAttempt(username, ipAddress, false);
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.isActive()) {
            loginAttemptDao.recordAttempt(username, ipAddress, false);
            throw new AuthenticationException("Account is deactivated. Please contact administrator.");
        }

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            loginAttemptDao.recordAttempt(username, ipAddress, false);
            throw new AuthenticationException("Invalid username or password");
        }

        // Record successful login
        loginAttemptDao.recordAttempt(username, ipAddress, true);
        loginAttemptDao.clearAttempts(username);

        logger.info("User {} logged in successfully", username);

        // Create login response
        return createLoginResponse(user);
    }

    /**
     * Change password for a user
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userDao.findById(userId);

        if (userOptional.isEmpty()) {
            throw new AuthenticationException("User not found");
        }

        User user = userOptional.get();

        // Verify old password
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Validate new password
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new ValidationException("New password must be at least 8 characters long");
        }

        // Hash and update new password
        String newPasswordHash = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDao.updatePassword(userId, newPasswordHash);

        if (updated) {
            logger.info("Password changed for user ID: {}", userId);
        }

        return updated;
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String username) {
        return userDao.existsByUsername(username);
    }

    /**
     * Validate login request
     */
    private void validateLoginRequest(LoginRequest loginRequest) {
        Map<String, String> errors = new HashMap<>();

        if (loginRequest == null) {
            throw new ValidationException("Login request cannot be null");
        }

        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            errors.put("username", "Username is required");
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            errors.put("password", "Password is required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Check if account is locked due to too many failed attempts
     */
    private boolean isAccountLocked(String username) {
        int recentFailedAttempts = loginAttemptDao.getRecentFailedAttempts(username, LOCK_DURATION_MINUTES);

        if (recentFailedAttempts >= MAX_LOGIN_ATTEMPTS) {
            logger.warn("Account locked for username: {} due to {} failed attempts",
                    username, recentFailedAttempts);
            return true;
        }

        return false;
    }

    /**
     * Create login response from user entity
     */
    private LoginResponse createLoginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .email(user.getEmail())
                .redirectUrl("dashboard.html")  // Use relative path instead of absolute
                .build();
    }

    /**
     * Get redirect URL based on user role
     */
    private String getRedirectUrlForRole(String role) {
        switch (role) {
            case "ADMIN":
                return "/sunrise-dental-clinic/dashboard.html";
            case "DENTIST":
                return "/sunrise-dental-clinic/dashboard.html";
            case "RECEPTIONIST":
                return "/sunrise-dental-clinic/dashboard.html";
            default:
                return "/sunrise-dental-clinic/dashboard.html";
        }
    }
}