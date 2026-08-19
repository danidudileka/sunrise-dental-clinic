package com.sunrisedental.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for date and time operations.
 */
public class DateUtil {
    private static final Logger logger = LogManager.getLogger(DateUtil.class);

    // Date and time formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Parse date string to LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;
            }
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Parse time string to LocalTime
     */
    public static LocalTime parseTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.trim().isEmpty()) {
                return null;
            }

            // Add seconds if missing
            if (timeStr.length() == 5) {
                timeStr += ":00";
            }

            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing time: {}", timeStr, e);
            return null;
        }
    }

    /**
     * Parse date-time string to LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date-time: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * Format date for database storage
     */
    public static String formatDateForDB(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Format date for display
     */
    public static String formatDateForDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : null;
    }

    /**
     * Format time for display
     */
    public static String formatTimeForDisplay(LocalTime time) {
        return time != null ? time.format(DISPLAY_TIME_FORMATTER) : null;
    }

    /**
     * Format date-time for display
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Check if date is today or in the future
     */
    public static boolean isTodayOrFuture(LocalDate date) {
        return date != null && (date.isEqual(LocalDate.now()) || date.isAfter(LocalDate.now()));
    }

    /**
     * Check if time is within business hours (8 AM to 8 PM)
     */
    public static boolean isWithinBusinessHours(LocalTime time) {
        if (time == null) {
            return false;
        }

        LocalTime businessStart = LocalTime.of(8, 0);
        LocalTime businessEnd = LocalTime.of(20, 0);

        return !time.isBefore(businessStart) && !time.isAfter(businessEnd);
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Get current date as string
     */
    public static String getCurrentDateString() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Get current time as string
     */
    public static String getCurrentTimeString() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    /**
     * Get current date-time as string
     */
    public static String getCurrentDateTimeString() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
}