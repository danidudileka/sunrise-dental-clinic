package com.sunrisedental.service;

import com.sunrisedental.dao.StaffDao;
import com.sunrisedental.dao.UserDao;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.Staff;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.ValidationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for user management operations.
 */
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDao userDao;
    private final StaffDao staffDao;

    public UserService() {
        this.userDao = new UserDao();
        this.staffDao = new StaffDao();
    }

    /**
     * Create new staff user (doctor or receptionist)
     */
    public Map<String, Object> createStaffUser(String username, String fullName, String email,
                                               String role, Integer dentistId) {
        // Validate input
        validateStaffData(username, fullName, email, role);

        // Check if username exists
        if (userDao.existsByUsername(username)) {
            throw new ValidationException("Username already exists");
        }

        // Generate random password
        String randomPassword = generateRandomPassword();
        String passwordHash = PasswordUtil.hashPassword(randomPassword);

        // Create user
        User user = User.builder()
                .username(username)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .email(email)
                .role(role)
                .isActive(true)
                .build();

        int userId = userDao.createUser(user);
        user.setUserId(userId);

        // Create staff record if dentist
        if ("DENTIST".equals(role) && dentistId != null) {
            Staff staff = Staff.builder()
                    .userId(userId)
                    .staffType("DENTIST")
                    .dentistId(dentistId)
                    .build();

            staffDao.createStaff(staff);
        } else if ("RECEPTIONIST".equals(role)) {
            Staff staff = Staff.builder()
                    .userId(userId)
                    .staffType("RECEPTIONIST")
                    .dentistId(null)
                    .build();

            staffDao.createStaff(staff);
        }

        logger.info("Created new {} user: {}", role, username);

        // Return user info with password
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("fullName", fullName);
        result.put("role", role);
        result.put("temporaryPassword", randomPassword);

        return result;
    }

    /**
     * Generate random password
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        // Generate 10 character password
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    /**
     * Validate staff data
     */
    private void validateStaffData(String username, String fullName, String email, String role) {
        Map<String, String> errors = new HashMap<>();

        if (username == null || !ValidationUtil.isValidUsername(username)) {
            errors.put("username", "Username must be 3-50 characters (letters, numbers, underscore)");
        }

        if (fullName == null || !ValidationUtil.isValidName(fullName)) {
            errors.put("fullName", "Valid full name is required");
        }

        if (email != null && !ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Invalid email address");
        }

        if (role == null || (!role.equals("DENTIST") && !role.equals("RECEPTIONIST"))) {
            errors.put("role", "Role must be DENTIST or RECEPTIONIST");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Get dentist ID by user ID
     */
    public Integer getDentistIdByUserId(int userId) {
        Optional<Staff> staffOptional = staffDao.findByUserId(userId);

        if (staffOptional.isPresent() && "DENTIST".equals(staffOptional.get().getStaffType())) {
            return staffOptional.get().getDentistId();
        }

        return null;
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
}