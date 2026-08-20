package com.sunrisedental.filter;

import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.util.JsonUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication filter to protect API endpoints.
 */
@WebFilter("/api/*")  // Only filter API endpoints, not HTML pages
public class AuthenticationFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(AuthenticationFilter.class);

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/help"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Allow access to public endpoints
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if user is authenticated
        if (isAuthenticated(httpRequest)) {
            chain.doFilter(request, response);
        } else {
            sendUnauthorizedResponse(httpResponse);
        }
    }

    private boolean isPublicEndpoint(String path) {
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (path.equals(publicEndpoint) || path.startsWith(publicEndpoint + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("username") != null;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse apiResponse = ApiResponse.builder()
                .status("ERROR")
                .message("Authentication required")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        try (PrintWriter writer = response.getWriter()) {
            writer.write(JsonUtil.toJson(apiResponse));
            writer.flush();
        }
    }

    @Override
    public void destroy() {
        logger.info("AuthenticationFilter destroyed");
    }
}