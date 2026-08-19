package com.sunrisedental.controller;

import com.sunrisedental.dto.request.AppointmentRequest;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.dto.response.AppointmentResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.service.AppointmentService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for appointment operations.
 * Handles appointment registration and retrieval.
 */
@WebServlet("/api/appointments/*")
public class AppointmentController extends BaseController {
    private final AppointmentService appointmentService;

    public AppointmentController() {
        this.appointmentService = new AppointmentService();
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
                case "":
                case "register":
                    handleRegisterAppointment(request, response);
                    break;

                case "status":
                    handleUpdateStatus(request, response);
                    break;

                case "cancel":
                    handleCancelAppointment(request, response);
                    break;

                case "complete":
                    handleCompleteAppointment(request, response);
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
            if (pathInfo.startsWith("number/")) {
                // Get by appointment number: /api/appointments/number/APT202400001
                String appointmentNumber = pathInfo.substring("number/".length());
                handleGetByNumber(request, response, appointmentNumber);

            } else if (pathInfo.startsWith("date/")) {
                // Get by date: /api/appointments/date/2024-01-15
                String date = pathInfo.substring("date/".length());
                handleGetByDate(request, response, date);

            } else if (pathInfo.startsWith("patient/")) {
                // Get by patient ID: /api/appointments/patient/1
                String patientIdStr = pathInfo.substring("patient/".length());
                handleGetByPatient(request, response, patientIdStr);

            } else if (pathInfo.startsWith("contact/")) {
                // Search by contact number: /api/appointments/contact/+94-77-1234567
                String contactNumber = pathInfo.substring("contact/".length());
                handleSearchByContact(request, response, contactNumber);

            } else if (pathInfo.startsWith("name/")) {
                // Search by patient name: /api/appointments/name/John
                String patientName = pathInfo.substring("name/".length());
                handleSearchByName(request, response, patientName);

            } else if (pathInfo.equals("all") || pathInfo.isEmpty()) {
                // Get all appointments: /api/appointments/all
                handleGetAllAppointments(request, response);

            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    /**
     * Handle search by contact number
     */
    private void handleSearchByContact(HttpServletRequest request, HttpServletResponse response,
                                       String contactNumber) throws IOException {
        List<AppointmentResponse> appointments = appointmentService.searchByContactNumber(contactNumber);
        sendSuccess(response, "Appointments retrieved successfully", appointments);
    }

    /**
     * Handle search by patient name
     */
    private void handleSearchByName(HttpServletRequest request, HttpServletResponse response,
                                    String patientName) throws IOException {
        List<AppointmentResponse> appointments = appointmentService.searchByPatientName(patientName);
        sendSuccess(response, "Appointments retrieved successfully", appointments);
    }

    /**
     * Handle get all appointments
     */
    private void handleGetAllAppointments(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        sendSuccess(response, "Appointments retrieved successfully", appointments);
    }

    /**
     * Handle appointment registration
     */
    private void handleRegisterAppointment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        AppointmentRequest appointmentRequest = parseJsonBody(request, AppointmentRequest.class);

        if (appointmentRequest == null) {
            throw new ValidationException("Invalid appointment request");
        }

        Integer createdBy = getCurrentUserId(request);
        if (createdBy == null) {
            throw new AuthenticationException("User not authenticated");
        }

        AppointmentResponse appointmentResponse = appointmentService.registerAppointment(
                appointmentRequest, createdBy);

        sendSuccess(response, "Appointment registered successfully", appointmentResponse);
    }

    /**
     * Handle get appointment by number
     */
    private void handleGetByNumber(HttpServletRequest request, HttpServletResponse response,
                                   String appointmentNumber) throws IOException {
        AppointmentResponse appointmentResponse = appointmentService.getAppointmentByNumber(
                appointmentNumber);

        sendSuccess(response, "Appointment retrieved successfully", appointmentResponse);
    }

    /**
     * Handle get appointments by date
     */
    private void handleGetByDate(HttpServletRequest request, HttpServletResponse response,
                                 String date) throws IOException {
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDate(date);

        sendSuccess(response, "Appointments retrieved successfully", appointments);
    }

    /**
     * Handle get appointments by patient
     */
    private void handleGetByPatient(HttpServletRequest request, HttpServletResponse response,
                                    String patientIdStr) throws IOException {
        try {
            int patientId = Integer.parseInt(patientIdStr);
            List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatient(patientId);

            sendSuccess(response, "Appointments retrieved successfully", appointments);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid patient ID");
        }
    }

    /**
     * Handle update appointment status
     */
    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        java.util.Map<String, String> statusData = parseJsonBody(request, java.util.Map.class);

        if (statusData == null) {
            throw new ValidationException("Invalid request");
        }

        String appointmentNumber = statusData.get("appointmentNumber");
        String status = statusData.get("status");

        if (appointmentNumber == null || status == null) {
            throw new ValidationException("Appointment number and status are required");
        }

        boolean updated = appointmentService.updateAppointmentStatus(appointmentNumber, status);

        if (updated) {
            sendSuccess(response, "Appointment status updated successfully", null);
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to update status");
        }
    }

    /**
     * Handle cancel appointment
     */
    private void handleCancelAppointment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        java.util.Map<String, String> cancelData = parseJsonBody(request, java.util.Map.class);

        if (cancelData == null) {
            throw new ValidationException("Invalid request");
        }

        String appointmentNumber = cancelData.get("appointmentNumber");

        if (appointmentNumber == null) {
            throw new ValidationException("Appointment number is required");
        }

        boolean cancelled = appointmentService.cancelAppointment(appointmentNumber);

        if (cancelled) {
            sendSuccess(response, "Appointment cancelled successfully", null);
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to cancel appointment");
        }
    }

    /**
     * Handle complete appointment
     */
    private void handleCompleteAppointment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        java.util.Map<String, String> completeData = parseJsonBody(request, java.util.Map.class);

        if (completeData == null) {
            throw new ValidationException("Invalid request");
        }

        String appointmentNumber = completeData.get("appointmentNumber");

        if (appointmentNumber == null) {
            throw new ValidationException("Appointment number is required");
        }

        boolean completed = appointmentService.completeAppointment(appointmentNumber);

        if (completed) {
            sendSuccess(response, "Appointment completed successfully", null);
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to complete appointment");
        }
    }
}