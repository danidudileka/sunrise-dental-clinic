package com.sunrisedental.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Application configuration constants and settings.
 */
public class AppConfig {

    // Application settings
    public static final String APP_NAME = "Sunrise Dental Clinic Management System";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_CONTEXT_PATH = "/sunrise-dental-clinic";

    // Session settings
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final String SESSION_USER_KEY = "loggedInUser";
    public static final String SESSION_USERNAME_KEY = "username";
    public static final String SESSION_ROLE_KEY = "userRole";
    public static final String SESSION_USER_ID_KEY = "userId";

    // Login settings
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCK_DURATION_MINUTES = 15;

    // Appointment settings
    public static final String APPOINTMENT_NUMBER_PREFIX = "APT";
    public static final String BILL_NUMBER_PREFIX = "BILL";

    // Business rules
    public static final double DEFAULT_CONSULTATION_FEE = 500.00;
    public static final double EMERGENCY_CONSULTATION_FEE = 1000.00;
    public static final double DISCOUNT_THRESHOLD = 10000.00;
    public static final double DISCOUNT_PERCENTAGE = 0.05; // 5% discount for bills over threshold

    // API Response codes
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_WARNING = "WARNING";

    // Role definitions
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_RECEPTIONIST = "RECEPTIONIST";
    public static final String ROLE_DENTIST = "DENTIST";

    // API endpoints
    public static final String API_BASE_PATH = "/api";
    public static final String API_AUTH_PATH = "/api/auth";
    public static final String API_APPOINTMENT_PATH = "/api/appointments";
    public static final String API_PATIENT_PATH = "/api/patients";
    public static final String API_BILLING_PATH = "/api/billing";
    public static final String API_REPORT_PATH = "/api/reports";
    public static final String API_HELP_PATH = "/api/help";

    // Frontend page paths
    public static final String PAGE_LOGIN = "/login.html";
    public static final String PAGE_DASHBOARD = "/dashboard.html";
    public static final String PAGE_APPOINTMENT = "/appointment.html";
    public static final String PAGE_BILLING = "/billing.html";
    public static final String PAGE_REPORTS = "/reports.html";
    public static final String PAGE_HELP = "/help.html";
    public static final String PAGE_ERROR = "/error.html";

    // Map of appointment status codes to display text
    public static final Map<String, String> APPOINTMENT_STATUS_MAP = new HashMap<>();
    static {
        APPOINTMENT_STATUS_MAP.put("SCHEDULED", "Scheduled");
        APPOINTMENT_STATUS_MAP.put("COMPLETED", "Completed");
        APPOINTMENT_STATUS_MAP.put("CANCELLED", "Cancelled");
        APPOINTMENT_STATUS_MAP.put("NO_SHOW", "No Show");
    }

    // Map of payment status codes to display text
    public static final Map<String, String> PAYMENT_STATUS_MAP = new HashMap<>();
    static {
        PAYMENT_STATUS_MAP.put("PENDING", "Pending");
        PAYMENT_STATUS_MAP.put("PAID", "Paid");
        PAYMENT_STATUS_MAP.put("PARTIALLY_PAID", "Partially Paid");
        PAYMENT_STATUS_MAP.put("REFUNDED", "Refunded");
    }

    // Map of payment method codes to display text
    public static final Map<String, String> PAYMENT_METHOD_MAP = new HashMap<>();
    static {
        PAYMENT_METHOD_MAP.put("CASH", "Cash");
        PAYMENT_METHOD_MAP.put("CARD", "Card");
        PAYMENT_METHOD_MAP.put("INSURANCE", "Insurance");
        PAYMENT_METHOD_MAP.put("ONLINE", "Online Transfer");
    }
}