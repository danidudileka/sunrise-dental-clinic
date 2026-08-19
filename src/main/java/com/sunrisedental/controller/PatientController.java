package com.sunrisedental.controller;

import com.sunrisedental.dto.request.PatientRequest;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.dto.response.PatientResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.service.PatientService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for patient operations.
 * Handles patient registration and retrieval.
 */
@WebServlet("/api/patients/*")
public class PatientController extends BaseController {
    private final PatientService patientService;

    public PatientController() {
        this.patientService = new PatientService();
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
                    handleRegisterPatient(request, response);
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
            if (pathInfo.isEmpty()) {
                // Get all patients: /api/patients
                handleGetAllPatients(request, response);

            } else if (pathInfo.startsWith("search/")) {
                // Search by name: /api/patients/search/john
                String searchTerm = pathInfo.substring("search/".length());
                handleSearchPatients(request, response, searchTerm);

            } else if (pathInfo.startsWith("contact/")) {
                // Get by contact number: /api/patients/contact/+94-77-1234567
                String contactNumber = pathInfo.substring("contact/".length());
                handleGetByContactNumber(request, response, contactNumber);

            } else {
                // Try to parse as patient ID
                try {
                    int patientId = Integer.parseInt(pathInfo);
                    handleGetPatientById(request, response, patientId);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                }
            }

        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            // Update patient: /api/patients/{id}
            try {
                int patientId = Integer.parseInt(pathInfo);
                handleUpdatePatient(request, response, patientId);
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID");
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            // Deactivate patient: /api/patients/{id}
            try {
                int patientId = Integer.parseInt(pathInfo);
                handleDeactivatePatient(request, response, patientId);
            } catch (NumberFormatException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID");
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    /**
     * Handle patient registration
     */
    private void handleRegisterPatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PatientRequest patientRequest = parseJsonBody(request, PatientRequest.class);

        if (patientRequest == null) {
            throw new ValidationException("Invalid patient request");
        }

        PatientResponse patientResponse = patientService.registerPatient(patientRequest);

        sendSuccess(response, "Patient registered successfully", patientResponse);
    }

    /**
     * Handle get patient by ID
     */
    private void handleGetPatientById(HttpServletRequest request, HttpServletResponse response,
                                      int patientId) throws IOException {
        PatientResponse patientResponse = patientService.getPatientById(patientId);

        sendSuccess(response, "Patient retrieved successfully", patientResponse);
    }

    /**
     * Handle get patient by contact number
     */
    private void handleGetByContactNumber(HttpServletRequest request, HttpServletResponse response,
                                          String contactNumber) throws IOException {
        PatientResponse patientResponse = patientService.getPatientByContactNumber(contactNumber);

        sendSuccess(response, "Patient retrieved successfully", patientResponse);
    }

    /**
     * Handle search patients
     */
    private void handleSearchPatients(HttpServletRequest request, HttpServletResponse response,
                                      String searchTerm) throws IOException {
        List<PatientResponse> patients = patientService.searchPatientsByName(searchTerm);

        sendSuccess(response, "Patients retrieved successfully", patients);
    }

    /**
     * Handle get all patients
     */
    private void handleGetAllPatients(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<PatientResponse> patients = patientService.getAllPatients();

        sendSuccess(response, "Patients retrieved successfully", patients);
    }

    /**
     * Handle update patient
     */
    private void handleUpdatePatient(HttpServletRequest request, HttpServletResponse response,
                                     int patientId) throws IOException {
        PatientRequest patientRequest = parseJsonBody(request, PatientRequest.class);

        if (patientRequest == null) {
            throw new ValidationException("Invalid patient request");
        }

        PatientResponse patientResponse = patientService.updatePatient(patientId, patientRequest);

        sendSuccess(response, "Patient updated successfully", patientResponse);
    }

    /**
     * Handle deactivate patient
     */
    private void handleDeactivatePatient(HttpServletRequest request, HttpServletResponse response,
                                         int patientId) throws IOException {
        boolean deactivated = patientService.deactivatePatient(patientId);

        if (deactivated) {
            sendSuccess(response, "Patient deactivated successfully", null);
        } else {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to deactivate patient");
        }
    }
}