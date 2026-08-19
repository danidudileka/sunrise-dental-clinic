package com.sunrisedental.controller;

import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.dto.response.ReportResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.service.ReportService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for report operations.
 * Handles report generation and analytics.
 */
@WebServlet("/api/reports/*")
public class ReportController extends BaseController {
    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            if (pathInfo.isEmpty() || pathInfo.equals("summary")) {
                // Get comprehensive report: /api/reports/summary?startDate=2024-01-01&endDate=2024-01-31
                handleGetSummaryReport(request, response);

            } else if (pathInfo.equals("dashboard")) {
                // Get dashboard summary: /api/reports/dashboard
                handleGetDashboardSummary(request, response);

            } else if (pathInfo.equals("daily")) {
                // Get daily appointments: /api/reports/daily?date=2024-01-15
                handleGetDailyAppointments(request, response);

            } else if (pathInfo.equals("revenue")) {
                // Get revenue report: /api/reports/revenue?startDate=2024-01-01&endDate=2024-01-31
                handleGetRevenueReport(request, response);

            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    /**
     * Handle get summary report
     */
    private void handleGetSummaryReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }

        ReportResponse report = reportService.generateReport(startDate, endDate);

        sendSuccess(response, "Report generated successfully", report);
    }

    /**
     * Handle get dashboard summary
     */
    private void handleGetDashboardSummary(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, Object> summary = reportService.getDashboardSummary();

        sendSuccess(response, "Dashboard summary retrieved successfully", summary);
    }

    /**
     * Handle get daily appointments
     */
    private void handleGetDailyAppointments(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String date = request.getParameter("date");

        if (date == null) {
            throw new ValidationException("Date is required");
        }

        List<Map<String, Object>> dailyAppointments = reportService.getDailyAppointmentsReport(date);

        sendSuccess(response, "Daily appointments retrieved successfully", dailyAppointments);
    }

    /**
     * Handle get revenue report
     */
    private void handleGetRevenueReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }

        List<Map<String, Object>> revenueReport = reportService.getRevenueReport(startDate, endDate);

        sendSuccess(response, "Revenue report retrieved successfully", revenueReport);
    }
}