package com.sunrisedental.controller;

import com.sunrisedental.dto.request.BillingRequest;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.dto.response.BillResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.service.BillingService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for billing operations.
 * Handles bill generation and payment processing.
 */
@WebServlet("/api/billing/*")
public class BillingController extends BaseController {
    private final BillingService billingService;

    public BillingController() {
        this.billingService = new BillingService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "generate":
                    handleGenerateBill(request, response);
                    break;

                case "calculate":
                    handleCalculateBill(request, response);
                    break;

                case "payment":
                    handleProcessPayment(request, response);
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
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            if (pathInfo.startsWith("bill/")) {
                // Get by bill number: /api/billing/bill/BILL000001
                String billNumber = pathInfo.substring("bill/".length());
                handleGetBillByNumber(request, response, billNumber);

            } else if (pathInfo.startsWith("appointment/")) {
                // Get by appointment number: /api/billing/appointment/APT202600001
                String appointmentNumber = pathInfo.substring("appointment/".length());
                handleGetBillByAppointment(request, response, appointmentNumber);

            } else if (pathInfo.startsWith("range/")) {
                // Get by date range: /api/billing/range/2024-01-01/2024-01-31
                String[] parts = pathInfo.substring("range/".length()).split("/");
                if (parts.length == 2) {
                    handleGetBillsByDateRange(request, response, parts[0], parts[1]);
                } else {
                    throw new ValidationException("Invalid date range format");
                }

            } else if (pathInfo.startsWith("revenue/")) {
                // Get revenue: /api/billing/revenue/2024-01-01/2024-01-31
                String[] parts = pathInfo.substring("revenue/".length()).split("/");
                if (parts.length == 2) {
                    handleGetRevenue(request, response, parts[0], parts[1]);
                } else {
                    throw new ValidationException("Invalid date range format");
                }

            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    /**
     * Handle bill generation
     */
    private void handleGenerateBill(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, String> billData = parseJsonBody(request, Map.class);

        if (billData == null) {
            throw new ValidationException("Invalid request");
        }

        String appointmentNumber = billData.get("appointmentNumber");

        if (appointmentNumber == null) {
            throw new ValidationException("Appointment number is required");
        }

        Integer createdBy = getCurrentUserId(request);
        if (createdBy == null) {
            throw new AuthenticationException("User not authenticated");
        }

        BillResponse billResponse = billingService.generateBill(appointmentNumber, createdBy);

        sendSuccess(response, "Bill generated successfully", billResponse);
    }

    /**
     * Handle bill calculation (preview)
     */
    private void handleCalculateBill(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, String> billData = parseJsonBody(request, Map.class);

        if (billData == null) {
            throw new ValidationException("Invalid request");
        }

        String appointmentNumber = billData.get("appointmentNumber");

        if (appointmentNumber == null) {
            throw new ValidationException("Appointment number is required");
        }

        BillResponse billResponse = billingService.calculateBill(appointmentNumber);

        sendSuccess(response, "Bill calculated successfully", billResponse);
    }

    /**
     * Handle payment processing
     */
    private void handleProcessPayment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, String> paymentData = parseJsonBody(request, Map.class);

        if (paymentData == null) {
            throw new ValidationException("Invalid request");
        }

        String billNumber = paymentData.get("billNumber");
        String paymentMethod = paymentData.get("paymentMethod");

        if (billNumber == null || paymentMethod == null) {
            throw new ValidationException("Bill number and payment method are required");
        }

        BillResponse billResponse = billingService.processPayment(billNumber, paymentMethod);

        sendSuccess(response, "Payment processed successfully", billResponse);
    }

    /**
     * Handle get bill by number
     */
    private void handleGetBillByNumber(HttpServletRequest request, HttpServletResponse response,
                                       String billNumber) throws IOException {
        BillResponse billResponse = billingService.getBillByNumber(billNumber);

        sendSuccess(response, "Bill retrieved successfully", billResponse);
    }

    /**
     * Handle get bill by appointment number
     */
    private void handleGetBillByAppointment(HttpServletRequest request, HttpServletResponse response,
                                            String appointmentNumber) throws IOException {
        BillResponse billResponse = billingService.getBillByAppointmentNumber(appointmentNumber);

        sendSuccess(response, "Bill retrieved successfully", billResponse);
    }

    /**
     * Handle get bills by date range
     */
    private void handleGetBillsByDateRange(HttpServletRequest request, HttpServletResponse response,
                                           String startDate, String endDate) throws IOException {
        List<BillResponse> bills = billingService.getBillsByDateRange(startDate, endDate);

        sendSuccess(response, "Bills retrieved successfully", bills);
    }

    /**
     * Handle get revenue
     */
    private void handleGetRevenue(HttpServletRequest request, HttpServletResponse response,
                                  String startDate, String endDate) throws IOException {
        BigDecimal revenue = billingService.getTotalRevenue(startDate, endDate);

        Map<String, Object> revenueData = new java.util.HashMap<>();
        revenueData.put("startDate", startDate);
        revenueData.put("endDate", endDate);
        revenueData.put("totalRevenue", revenue);

        sendSuccess(response, "Revenue retrieved successfully", revenueData);
    }
}