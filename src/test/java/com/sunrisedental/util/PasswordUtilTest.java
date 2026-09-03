package com.sunrisedental.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test automation for PasswordUtil
 * Tests password hashing, verification, and strength validation
 */
class PasswordUtilTest {

    @Test
    @DisplayName("TC-PWD-001: Hash password produces valid BCrypt hash")
    void testHashPassword() {
        // Act
        String hashedPassword = PasswordUtil.hashPassword("TestPassword123!");

        // Assert
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertTrue(hashedPassword.startsWith("$2a$"), "Should be BCrypt hash");
        assertNotEquals("TestPassword123!", hashedPassword, "Hash should differ from plaintext");
        assertEquals(60, hashedPassword.length(), "BCrypt hash should be 60 characters");
    }

    @Test
    @DisplayName("TC-PWD-002: Verify correct password")
    void testVerifyCorrectPassword() {
        // Arrange
        String password = "MySecurePass123!";
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Act
        boolean result = PasswordUtil.verifyPassword(password, hashedPassword);

        // Assert
        assertTrue(result, "Correct password should verify successfully");
    }

    @Test
    @DisplayName("TC-PWD-003: Reject incorrect password")
    void testVerifyIncorrectPassword() {
        // Arrange
        String hashedPassword = PasswordUtil.hashPassword("CorrectPassword123!");

        // Act
        boolean result = PasswordUtil.verifyPassword("WrongPassword456!", hashedPassword);

        // Assert
        assertFalse(result, "Incorrect password should fail verification");
    }

    @Test
    @DisplayName("TC-PWD-004: Hash null password throws exception")
    void testHashNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(null);
        }, "Should throw IllegalArgumentException for null password");
    }

    @Test
    @DisplayName("TC-PWD-005: Hash empty password throws exception")
    void testHashEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("");
        }, "Should throw IllegalArgumentException for empty password");
    }

    @Test
    @DisplayName("TC-PWD-006: Verify null passwords return false")
    void testVerifyNullPasswords() {
        assertFalse(PasswordUtil.verifyPassword(null, "hashed"));
        assertFalse(PasswordUtil.verifyPassword("password", null));
        assertFalse(PasswordUtil.verifyPassword(null, null));
    }

    @Test
    @DisplayName("TC-PWD-007: Strong password passes validation")
    void testStrongPassword() {
        assertTrue(PasswordUtil.isStrongPassword("StrongPass123!"));
        assertTrue(PasswordUtil.isStrongPassword("Secure@2026Pass"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"weak", "OnlyLetters", "12345678", "NoSpecialChar1"})
    @DisplayName("TC-PWD-008: Weak password fails validation")
    void testWeakPassword(String password) {
        assertFalse(PasswordUtil.isStrongPassword(password));
    }

    @Test
    @DisplayName("TC-PWD-009: Generate temporary password")
    void testGenerateTemporaryPassword() {
        // Act
        String tempPassword = PasswordUtil.generateTemporaryPassword(12);

        // Assert
        assertNotNull(tempPassword);
        assertEquals(12, tempPassword.length());
    }

    @Test
    @DisplayName("TC-PWD-010: Temporary password is different each time")
    void testTemporaryPasswordRandom() {
        // Act
        String password1 = PasswordUtil.generateTemporaryPassword(10);
        String password2 = PasswordUtil.generateTemporaryPassword(10);

        // Assert
        assertNotEquals(password1, password2, "Temporary passwords should be random");
    }
}