package com.sunrisedental.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for input validation.
 * Provides centralized validation methods for all user inputs.
 */
public class ValidationUtil {
    private static final Logger logger = LogManager.getLogger(ValidationUtil.class);

    // Regular expression patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\-\\s]{10,20}$");
    private static final Pattern APPOINTMENT_NUMBER_PATTERN = Pattern.compile("^APT[0-9]{9}$");
    private static final Pattern BILL_NUMBER_PATTERN = Pattern.compile("^BILL[0-9]{6}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,100}$");

    /**
     * Validate username
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validate password (minimum 8 characters)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Validate appointment number format
     */
    public static boolean isValidAppointmentNumber(String appointmentNumber) {
        if (appointmentNumber == null) {
            return false;
        }
        return APPOINTMENT_NUMBER_PATTERN.matcher(appointmentNumber.trim()).matches();
    }

    /**
     * Validate bill number format
     */
    public static boolean isValidBillNumber(String billNumber) {
        if (billNumber == null) {
            return false;
        }
        return BILL_NUMBER_PATTERN.matcher(billNumber.trim()).matches();
    }

    /**
     * Validate name (letters and spaces only, 2-100 characters)
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate date string (yyyy-MM-dd format)
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) {
            return false;
        }

        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            logger.debug("Invalid date format: {}", dateStr);
            return false;
        }
    }

    /**
     * Validate time string (HH:mm:ss or HH:mm format)
     */
    public static boolean isValidTime(String timeStr) {
        if (timeStr == null) {
            return false;
        }

        try {
            if (timeStr.length() == 5) {
                timeStr += ":00"; // Add seconds if missing
            }
            LocalTime.parse(timeStr);
            return true;
        } catch (DateTimeParseException e) {
            logger.debug("Invalid time format: {}", timeStr);
            return false;
        }
    }

    /**
     * Validate future date
     */
    public static boolean isFutureDate(String dateStr) {
        if (!isValidDate(dateStr)) {
            return false;
        }

        LocalDate date = LocalDate.parse(dateStr);
        return date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now());
    }

    /**
     * Validate amount (positive decimal number)
     */
    public static boolean isValidAmount(double amount) {
        return amount >= 0 && amount <= 1000000; // Max 1 million
    }

    /**
     * Validate amount string
     */
    public static boolean isValidAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return isValidAmount(amount);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate address (minimum 5 characters)
     */
    public static boolean isValidAddress(String address) {
        return address != null && address.trim().length() >= 5 && address.trim().length() <= 500;
    }

    /**
     * Sanitize input to prevent XSS attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Validate all required fields for appointment
     */
    public static String validateAppointment(String appointmentNumber, String patientName,
                                             String contactNumber, String dentistName,
                                             String treatmentType, String appointmentDate,
                                             String appointmentTime) {
        StringBuilder errors = new StringBuilder();

        if (!isValidAppointmentNumber(appointmentNumber)) {
            errors.append("Invalid appointment number format. ");
        }
        if (!isValidName(patientName)) {
            errors.append("Invalid patient name. ");
        }
        if (!isValidPhoneNumber(contactNumber)) {
            errors.append("Invalid contact number. ");
        }
        if (!isValidName(dentistName)) {
            errors.append("Invalid dentist name. ");
        }
        if (!isValidName(treatmentType)) {
            errors.append("Invalid treatment type. ");
        }
        if (!isFutureDate(appointmentDate)) {
            errors.append("Appointment date must be today or in the future. ");
        }
        if (!isValidTime(appointmentTime)) {
            errors.append("Invalid appointment time. ");
        }

        return errors.toString();
    }
}