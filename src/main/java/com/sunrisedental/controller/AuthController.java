package com.sunrisedental.controller;

import com.sunrisedental.dto.request.LoginRequest;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.dto.response.LoginResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.service.AuthService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication operations.
 * Handles login, logout, and session management.
 */
@WebServlet("/api/auth/*")
public class AuthController extends BaseController {
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "login":
                    handleLogin(request, response);
                    break;

                case "logout":
                    handleLogout(request, response);
                    break;

                case "change-password":
                    handleChangePassword(request, response);
                    break;

                default:
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "session":
                    checkSession(request, response);
                    break;

                case "user-info":
                    getUserInfo(request, response);
                    break;

                default:
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    /**
     * Handle login request
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        LoginRequest loginRequest = parseJsonBody(request, LoginRequest.class);

        if (loginRequest == null) {
            throw new ValidationException("Invalid login request");
        }

        String ipAddress = getClientIpAddress(request);
        LoginResponse loginResponse = authService.login(loginRequest, ipAddress);

        // Create session
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", loginResponse.getUserId());
        session.setAttribute("username", loginResponse.getUsername());
        session.setAttribute("fullName", loginResponse.getFullName());
        session.setAttribute("userRole", loginResponse.getRole());
        session.setAttribute("email", loginResponse.getEmail());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Set session ID in response
        loginResponse.setSessionId(session.getId());

        sendSuccess(response, "Login successful", loginResponse);
    }

    /**
     * Handle logout request
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            logger.info("User {} logged out", username);
        }

        Map<String, String> data = new HashMap<>();
        data.put("redirectUrl", "/login.html");

        sendSuccess(response, "Logout successful", data);
    }

    /**
     * Handle password change
     */
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            throw new AuthenticationException("User not authenticated");
        }

        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            throw new AuthenticationException("User not authenticated");
        }

        Map<String, String> passwordData = parseJsonBody(request, Map.class);

        if (passwordData == null) {
            throw new ValidationException("Invalid request");
        }

        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            throw new ValidationException("Old password and new password are required");
        }

        boolean changed = authService.changePassword(userId, oldPassword, newPassword);

        if (changed) {
            sendSuccess(response, "Password changed successfully", null);
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to change password");
        }
    }

    /**
     * Check if session is valid
     */
    private void checkSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        boolean authenticated = isAuthenticated(request);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("authenticated", authenticated);

        if (authenticated) {
            sessionData.put("username", getCurrentUsername(request));
            sessionData.put("role", getCurrentUserRole(request));
            sessionData.put("userId", getCurrentUserId(request));
        }

        sendSuccess(response, "Session check completed", sessionData);
    }

    /**
     * Get current user information
     */
    private void getUserInfo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            throw new AuthenticationException("User not authenticated");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", getCurrentUserId(request));
        userInfo.put("username", getCurrentUsername(request));
        userInfo.put("role", getCurrentUserRole(request));

        HttpSession session = request.getSession(false);
        if (session != null) {
            userInfo.put("fullName", session.getAttribute("fullName"));
            userInfo.put("email", session.getAttribute("email"));
        }

        sendSuccess(response, "User information retrieved", userInfo);
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // In case of multiple IPs (X-Forwarded-For), take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}