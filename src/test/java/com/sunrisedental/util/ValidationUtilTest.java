package com.sunrisedental.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    @DisplayName("TC-VAL-001: Validate correct username")
    void testValidUsername() {
        assertTrue(ValidationUtil.isValidUsername("john_doe"));
        assertTrue(ValidationUtil.isValidUsername("admin123"));
        assertTrue(ValidationUtil.isValidUsername("user_name"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "user@name", "user.name"})
    @DisplayName("TC-VAL-002: Validate incorrect username")
    void testInvalidUsername(String username) {
        assertFalse(ValidationUtil.isValidUsername(username));
    }

    @Test
    @DisplayName("TC-VAL-003: Validate correct phone numbers")
    void testValidPhoneNumber() {
        assertTrue(ValidationUtil.isValidPhoneNumber("+94-77-1234567"));
        assertTrue(ValidationUtil.isValidPhoneNumber("0771234567"));
        assertTrue(ValidationUtil.isValidPhoneNumber("0112345678"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "abcdefghij", "123456789012345678901"})
    @DisplayName("TC-VAL-004: Validate incorrect phone numbers")
    void testInvalidPhoneNumber(String phone) {
        assertFalse(ValidationUtil.isValidPhoneNumber(phone));
    }

    @Test
    @DisplayName("TC-VAL-005: Validate correct email addresses")
    void testValidEmail() {
        assertTrue(ValidationUtil.isValidEmail("john.doe@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user@domain.lk"));
        assertTrue(ValidationUtil.isValidEmail("test123@gmail.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid.email", "@domain.com", "user@"})
    @DisplayName("TC-VAL-006: Validate incorrect email addresses")
    void testInvalidEmail(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @Test
    @DisplayName("TC-VAL-007: Validate correct appointment number")
    void testValidAppointmentNumber() {
        assertTrue(ValidationUtil.isValidAppointmentNumber("APT202600001"));
        assertTrue(ValidationUtil.isValidAppointmentNumber("APT000000001"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"APT123", "XYZ202600001", "APT20260001", "202600001"})
    @DisplayName("TC-VAL-008: Validate incorrect appointment number")
    void testInvalidAppointmentNumber(String appointmentNumber) {
        assertFalse(ValidationUtil.isValidAppointmentNumber(appointmentNumber));
    }

    @Test
    @DisplayName("TC-VAL-009: Validate correct dates")
    void testValidDate() {
        assertTrue(ValidationUtil.isValidDate("2026-01-15"));
        assertTrue(ValidationUtil.isValidDate("2026-12-31"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"15-01-2026", "2026-13-01", "2026-01-32", "2026/01/15"})
    @DisplayName("TC-VAL-010: Validate incorrect dates")
    void testInvalidDate(String date) {
        assertFalse(ValidationUtil.isValidDate(date));
    }

    @Test
    @DisplayName("TC-VAL-011: Validate correct times")
    void testValidTime() {
        assertTrue(ValidationUtil.isValidTime("09:00:00"));
        assertTrue(ValidationUtil.isValidTime("14:30"));
        assertTrue(ValidationUtil.isValidTime("23:59:59"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"25:00", "09:60", "9:00", "0900"})
    @DisplayName("TC-VAL-012: Validate incorrect times")
    void testInvalidTime(String time) {
        assertFalse(ValidationUtil.isValidTime(time));
    }

    @Test
    @DisplayName("TC-VAL-013: Validate future dates")
    void testFutureDate() {
        String futureDate = java.time.LocalDate.now().plusDays(7).toString();
        String today = java.time.LocalDate.now().toString();

        assertTrue(ValidationUtil.isFutureDate(futureDate));
        assertTrue(ValidationUtil.isFutureDate(today));
    }

    @Test
    @DisplayName("TC-VAL-014: Validate past dates rejected")
    void testPastDateRejected() {
        String pastDate = java.time.LocalDate.now().minusDays(1).toString();
        assertFalse(ValidationUtil.isFutureDate(pastDate));
    }

    @Test
    @DisplayName("TC-VAL-015: Validate amounts")
    void testValidAmount() {
        assertTrue(ValidationUtil.isValidAmount(0));
        assertTrue(ValidationUtil.isValidAmount(100.50));
        assertTrue(ValidationUtil.isValidAmount(1000000));
        assertFalse(ValidationUtil.isValidAmount(-100));
        assertFalse(ValidationUtil.isValidAmount(1000001));
    }

    @Test
    @DisplayName("TC-VAL-016: Validate sanitize input")
    void testSanitizeInput() {
        assertEquals("&lt;script&gt;", ValidationUtil.sanitizeInput("<script>"));
        assertEquals("&amp;", ValidationUtil.sanitizeInput("&"));
        assertEquals("&quot;", ValidationUtil.sanitizeInput("\""));
        assertEquals(null, ValidationUtil.sanitizeInput(null));
        assertEquals("Normal text", ValidationUtil.sanitizeInput("Normal text"));
    }
}