package com.sunrisedental.controller;

import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.service.UserService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for user management operations.
 * Admin-only endpoints for staff registration.
 */
@WebServlet("/api/users/*")
public class UserController extends BaseController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        // Check if user is admin
        if (!hasRole(request, "ADMIN")) {
            handleException(request, response, new AuthenticationException("Admin access required"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "create-staff":
                    handleCreateStaff(request, response);
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
     * Handle staff creation
     */
    private void handleCreateStaff(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> staffData = parseJsonBody(request, Map.class);

        if (staffData == null) {
            throw new ValidationException("Invalid request");
        }

        String username = (String) staffData.get("username");
        String fullName = (String) staffData.get("fullName");
        String email = (String) staffData.get("email");
        String role = (String) staffData.get("role");
        Integer dentistId = staffData.get("dentistId") != null ?
                Integer.parseInt(staffData.get("dentistId").toString()) : null;

        Map<String, Object> result = userService.createStaffUser(username, fullName, email, role, dentistId);

        sendSuccess(response, "Staff user created successfully", result);
    }
}