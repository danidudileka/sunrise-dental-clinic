package com.sunrisedental.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Logging filter to log all incoming requests.
 * Records request details and response time.
 */
@WebFilter("/*")
public class LoggingFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Logging Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        // Log request details
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String remoteAddr = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        logger.debug("Request: {} {} {} from {} User-Agent: {}",
                method, uri, queryString != null ? "?" + queryString : "",
                remoteAddr, userAgent);

        try {
            // Continue with the request
            chain.doFilter(request, response);
        } finally {
            // Log response details
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            int statusCode = httpResponse.getStatus();

            logger.debug("Response: {} {} - Status: {} - Duration: {}ms",
                    method, uri, statusCode, duration);

            // Log slow requests
            if (duration > 1000) {
                logger.warn("Slow request: {} {} took {}ms", method, uri, duration);
            }
        }
    }

    @Override
    public void destroy() {
        logger.info("Logging Filter destroyed");
    }
}