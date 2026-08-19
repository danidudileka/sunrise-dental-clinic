package com.sunrisedental.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for password hashing and verification.
 * Uses BCrypt algorithm for secure password storage.
 */
public class PasswordUtil {
    private static final Logger logger = LogManager.getLogger(PasswordUtil.class);

    // BCrypt cost factor (4-31, higher is more secure but slower)
    private static final int BCRYPT_COST = 10;

    /**
     * Hash a password using BCrypt
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        String hashedPassword = BCrypt.withDefaults()
                .hashToString(BCRYPT_COST, plainPassword.toCharArray());

        logger.debug("Password hashed successfully");
        return hashedPassword;
    }

    /**
     * Verify a plain password against a hashed password
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        BCrypt.Result result = BCrypt.verifyer()
                .verify(plainPassword.toCharArray(), hashedPassword);

        boolean verified = result.verified;
        logger.debug("Password verification result: {}", verified);
        return verified;
    }

    /**
     * Validate password strength
     * Password must have at least 8 characters, one uppercase, one lowercase, one number
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    /**
     * Generate a temporary password for reset operations
     */
    public static String generateTemporaryPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();

        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }
}