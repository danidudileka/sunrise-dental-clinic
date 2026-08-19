package com.sunrisedental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.exception.GlobalExceptionHandler;
import com.sunrisedental.util.JsonUtil;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Base controller class for all servlet controllers.
 * Provides common methods for JSON handling and session management.
 */
public abstract class BaseController extends HttpServlet {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    /**
     * Send JSON response
     */
    protected void sendJsonResponse(HttpServletResponse response, int statusCode, ApiResponse apiResponse)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(JsonUtil.toJson(apiResponse));
            writer.flush();
        }
    }

    /**
     * Send success response
     */
    protected void sendSuccess(HttpServletResponse response, String message, Object data)
            throws IOException {
        ApiResponse apiResponse = ApiResponse.success(message, data);
        sendJsonResponse(response, HttpServletResponse.SC_OK, apiResponse);
    }

    /**
     * Send error response
     */
    protected void sendError(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        ApiResponse apiResponse = ApiResponse.error(message);
        sendJsonResponse(response, statusCode, apiResponse);
    }

    /**
     * Parse JSON request body
     */
    protected <T> T parseJsonBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        String jsonBody = stringBuilder.toString();
        if (jsonBody.isEmpty()) {
            return null;
        }

        return JsonUtil.fromJson(jsonBody, clazz);
    }

    /**
     * Get current user ID from session
     */
    protected Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("userId");
            if (userId instanceof Integer) {
                return (Integer) userId;
            }
        }
        return null;
    }

    /**
     * Get current username from session
     */
    protected String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute("username");
            if (username != null) {
                return username.toString();
            }
        }
        return null;
    }

    /**
     * Get current user role from session
     */
    protected String getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object role = session.getAttribute("userRole");
            if (role != null) {
                return role.toString();
            }
        }
        return null;
    }

    /**
     * Check if user is authenticated
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("username") != null;
    }

    /**
     * Check if user has required role
     */
    protected boolean hasRole(HttpServletRequest request, String requiredRole) {
        String userRole = getCurrentUserRole(request);
        return userRole != null && userRole.equals(requiredRole);
    }

    /**
     * Handle exceptions globally
     */
    protected void handleException(HttpServletRequest request, HttpServletResponse response, Exception e)
            throws IOException {
        GlobalExceptionHandler.handleException(request, response, e);
    }

    /**
     * Get request path info
     */
    protected String getPathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return "";
        }
        return pathInfo.substring(1); // Remove leading slash
    }

    /**
     * Extract ID from path
     */
    protected Integer extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}