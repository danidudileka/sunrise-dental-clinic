package com.sunrisedental.service;

import com.sunrisedental.dao.LoginAttemptDao;
import com.sunrisedental.dao.UserDao;
import com.sunrisedental.dto.request.LoginRequest;
import com.sunrisedental.dto.response.LoginResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test automation for AuthService
 * Tests login functionality, password management, and account lockout
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private LoginAttemptDao loginAttemptDao;

    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        authService = new AuthService();

        // Inject mocks using reflection
        Field userDaoField = AuthService.class.getDeclaredField("userDao");
        userDaoField.setAccessible(true);
        userDaoField.set(authService, userDao);

        Field loginAttemptDaoField = AuthService.class.getDeclaredField("loginAttemptDao");
        loginAttemptDaoField.setAccessible(true);
        loginAttemptDaoField.set(authService, loginAttemptDao);

        // Create test user
        testUser = User.builder()
                .userId(1)
                .username("testadmin")
                .passwordHash(PasswordUtil.hashPassword("testpass123"))
                .fullName("Test Admin")
                .email("test@example.com")
                .role("ADMIN")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("TC-AUTH-001: Verify successful login with valid credentials")
    void testSuccessfulLogin() {
        // Arrange
        LoginRequest request = new LoginRequest("testadmin", "testpass123");
        when(userDao.findByUsername("testadmin")).thenReturn(Optional.of(testUser));
        when(loginAttemptDao.getRecentFailedAttempts(anyString(), anyInt())).thenReturn(0);

        // Act
        LoginResponse response = authService.login(request, "127.0.0.1");

        // Assert
        assertNotNull(response, "Login response should not be null");
        assertEquals("testadmin", response.getUsername(), "Username should match");
        assertEquals("Test Admin", response.getFullName(), "Full name should match");
        assertEquals("ADMIN", response.getRole(), "Role should be ADMIN");
        assertEquals(1, response.getUserId(), "User ID should be 1");

        // Verify interactions
        verify(userDao).findByUsername("testadmin");
        verify(loginAttemptDao).recordAttempt("testadmin", "127.0.0.1", true);
        verify(loginAttemptDao).clearAttempts("testadmin");
    }

    @Test
    @DisplayName("TC-AUTH-002: Verify login fails with incorrect password")
    void testLoginWithWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("testadmin", "wrongpassword");
        when(userDao.findByUsername("testadmin")).thenReturn(Optional.of(testUser));
        when(loginAttemptDao.getRecentFailedAttempts(anyString(), anyInt())).thenReturn(0);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            authService.login(request, "127.0.0.1");
        }, "Should throw AuthenticationException for wrong password");

        // Verify failed attempt was recorded
        verify(loginAttemptDao).recordAttempt("testadmin", "127.0.0.1", false);
    }

    @Test
    @DisplayName("TC-AUTH-003: Verify login fails for non-existent user")
    void testLoginWithNonExistentUser() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent", "password");
        when(userDao.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            authService.login(request, "127.0.0.1");
        }, "Should throw AuthenticationException for non-existent user");
    }

    @Test
    @DisplayName("TC-AUTH-004: Verify account lockout after multiple failed attempts")
    void testAccountLockout() {
        // Arrange
        LoginRequest request = new LoginRequest("testadmin", "testpass123");
        when(loginAttemptDao.getRecentFailedAttempts("testadmin", 15)).thenReturn(5);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            authService.login(request, "127.0.0.1");
        }, "Should throw AuthenticationException for locked account");
    }

    @Test
    @DisplayName("TC-AUTH-005: Verify login with null request throws ValidationException")
    void testLoginWithNullRequest() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.login(null, "127.0.0.1");
        }, "Should throw ValidationException for null request");
    }

    @Test
    @DisplayName("TC-AUTH-006: Verify password change with correct old password")
    void testChangePasswordSuccess() {
        // Arrange
        when(userDao.findById(1)).thenReturn(Optional.of(testUser));
        when(userDao.updatePassword(anyInt(), anyString())).thenReturn(true);

        // Act
        boolean result = authService.changePassword(1, "testpass123", "newpass456");

        // Assert
        assertTrue(result, "Password change should be successful");
        verify(userDao).updatePassword(eq(1), anyString());
    }

    @Test
    @DisplayName("TC-AUTH-007: Verify password change fails with wrong old password")
    void testChangePasswordWrongOldPassword() {
        // Arrange
        when(userDao.findById(1)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            authService.changePassword(1, "wrongpassword", "newpass456");
        }, "Should throw AuthenticationException for wrong old password");
    }
}