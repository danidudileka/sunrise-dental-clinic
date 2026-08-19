package com.sunrisedental.controller;

import com.sunrisedental.dto.response.ApiResponse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for help section.
 * Provides step-by-step instructions for system usage.
 */
@WebServlet("/api/help/*")
public class HelpController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "":
                case "topics":
                    handleGetHelpTopics(request, response);
                    break;

                case "appointments":
                    handleGetAppointmentHelp(request, response);
                    break;

                case "billing":
                    handleGetBillingHelp(request, response);
                    break;

                case "reports":
                    handleGetReportsHelp(request, response);
                    break;

                default:
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Help topic not found");
                    break;
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    /**
     * Handle get all help topics
     */
    private void handleGetHelpTopics(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<Map<String, String>> topics = new ArrayList<>();

        topics.add(createTopic("appointments", "Managing Appointments",
                "How to register, view, and manage patient appointments"));
        topics.add(createTopic("billing", "Billing and Payments",
                "How to generate bills and process payments"));
        topics.add(createTopic("reports", "Reports and Analytics",
                "How to generate and view reports"));
        topics.add(createTopic("patients", "Patient Management",
                "How to register and manage patient information"));

        sendSuccess(response, "Help topics retrieved successfully", topics);
    }

    /**
     * Handle get appointment help
     */
    private void handleGetAppointmentHelp(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<Map<String, String>> steps = new ArrayList<>();

        steps.add(createStep("1", "Navigate to Appointments",
                "Click on the 'Appointments' menu item in the navigation bar"));
        steps.add(createStep("2", "Click 'New Appointment'",
                "Click the 'New Appointment' button to open the registration form"));
        steps.add(createStep("3", "Fill in Patient Details",
                "Enter patient name, address, contact number, and email"));
        steps.add(createStep("4", "Select Dentist and Treatment",
                "Choose the dentist and treatment type from the dropdown menus"));
        steps.add(createStep("5", "Set Date and Time",
                "Select appointment date and time (must be within business hours)"));
        steps.add(createStep("6", "Submit",
                "Click 'Register Appointment' to save the appointment"));
        steps.add(createStep("7", "View Appointment",
                "Search by appointment number to view appointment details"));

        Map<String, Object> helpData = new LinkedHashMap<>();
        helpData.put("title", "Managing Appointments");
        helpData.put("description", "Step-by-step guide for appointment management");
        helpData.put("steps", steps);

        sendSuccess(response, "Appointment help retrieved successfully", helpData);
    }

    /**
     * Handle get billing help
     */
    private void handleGetBillingHelp(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<Map<String, String>> steps = new ArrayList<>();

        steps.add(createStep("1", "Navigate to Billing",
                "Click on the 'Billing' menu item in the navigation bar"));
        steps.add(createStep("2", "Enter Appointment Number",
                "Enter the appointment number to generate a bill"));
        steps.add(createStep("3", "Preview Bill",
                "Click 'Calculate' to preview the bill with treatment cost and consultation fee"));
        steps.add(createStep("4", "Generate Bill",
                "Click 'Generate Bill' to create the official bill"));
        steps.add(createStep("5", "Process Payment",
                "Select payment method and click 'Process Payment' to complete the transaction"));
        steps.add(createStep("6", "Print Receipt",
                "Click 'Print' to print the bill/receipt for the patient"));

        Map<String, Object> helpData = new LinkedHashMap<>();
        helpData.put("title", "Billing and Payments");
        helpData.put("description", "Step-by-step guide for billing operations");
        helpData.put("steps", steps);

        sendSuccess(response, "Billing help retrieved successfully", helpData);
    }

    /**
     * Handle get reports help
     */
    private void handleGetReportsHelp(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<Map<String, String>> steps = new ArrayList<>();

        steps.add(createStep("1", "Navigate to Reports",
                "Click on the 'Reports' menu item in the navigation bar"));
        steps.add(createStep("2", "Select Report Type",
                "Choose from daily appointments, revenue, or summary reports"));
        steps.add(createStep("3", "Set Date Range",
                "Select start and end dates for the report"));
        steps.add(createStep("4", "Generate Report",
                "Click 'Generate' to create the report"));
        steps.add(createStep("5", "View Results",
                "Review the report data in tables and charts"));
        steps.add(createStep("6", "Export Report",
                "Click 'Export' to download the report as PDF or CSV"));

        Map<String, Object> helpData = new LinkedHashMap<>();
        helpData.put("title", "Reports and Analytics");
        helpData.put("description", "Step-by-step guide for report generation");
        helpData.put("steps", steps);

        sendSuccess(response, "Reports help retrieved successfully", helpData);
    }

    /**
     * Create help topic
     */
    private Map<String, String> createTopic(String id, String title, String description) {
        Map<String, String> topic = new LinkedHashMap<>();
        topic.put("id", id);
        topic.put("title", title);
        topic.put("description", description);
        return topic;
    }

    /**
     * Create help step
     */
    private Map<String, String> createStep(String number, String title, String description) {
        Map<String, String> step = new LinkedHashMap<>();
        step.put("step", number);
        step.put("title", title);
        step.put("description", description);
        return step;
    }
}