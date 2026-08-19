package com.sunrisedental.service;

import com.sunrisedental.dao.UserDao;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.ValidationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for user management operations.
 */
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    /**
     * Create new user
     */
    public User createUser(User user, String plainPassword) {
        validateUserData(user, plainPassword);

        // Check if username exists
        if (userDao.existsByUsername(user.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        // Hash password
        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        user.setPasswordHash(passwordHash);

        int userId = userDao.createUser(user);
        user.setUserId(userId);

        logger.info("Created new user with ID: {} and username: {}", userId, user.getUsername());
        return user;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        Optional<User> userOptional = userDao.findById(userId);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("User", String.valueOf(userId));
        }

        return userOptional.get();
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        Optional<User> userOptional = userDao.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("User", username);
        }

        return userOptional.get();
    }

    /**
     * Update user
     */
    public User updateUser(int userId, User user) {
        Optional<User> existingUser = userDao.findById(userId);

        if (existingUser.isEmpty()) {
            throw new NotFoundException("User", String.valueOf(userId));
        }

        user.setUserId(userId);
        boolean updated = userDao.updateUser(user);

        if (updated) {
            logger.info("Updated user with ID: {}", userId);
            return getUserById(userId);
        }

        throw new ValidationException("Failed to update user");
    }

    /**
     * Deactivate user
     */
    public boolean deactivateUser(int userId) {
        Optional<User> existingUser = userDao.findById(userId);

        if (existingUser.isEmpty()) {
            throw new NotFoundException("User", String.valueOf(userId));
        }

        return userDao.deactivateUser(userId);
    }

    /**
     * Validate user data
     */
    private void validateUserData(User user, String plainPassword) {
        Map<String, String> errors = new HashMap<>();

        if (user.getUsername() == null || !ValidationUtil.isValidUsername(user.getUsername())) {
            errors.put("username", "Username must be 3-50 characters (letters, numbers, underscore)");
        }

        if (plainPassword != null && !ValidationUtil.isValidPassword(plainPassword)) {
            errors.put("password", "Password must be at least 8 characters");
        }

        if (user.getFullName() == null || !ValidationUtil.isValidName(user.getFullName())) {
            errors.put("fullName", "Valid full name is required");
        }

        if (user.getEmail() != null && !ValidationUtil.isValidEmail(user.getEmail())) {
            errors.put("email", "Invalid email address");
        }

        if (user.getRole() == null || !isValidRole(user.getRole())) {
            errors.put("role", "Invalid role");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Check if role is valid
     */
    private boolean isValidRole(String role) {
        return role != null && (
                role.equals("ADMIN") ||
                        role.equals("RECEPTIONIST") ||
                        role.equals("DENTIST")
        );
    }
}