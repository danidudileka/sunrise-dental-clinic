package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dao.BillDao;
import com.sunrisedental.dto.response.BillResponse;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test automation for BillingService
 * Tests bill calculation, discount application, and payment processing
 */
@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillDao billDao;

    @Mock
    private AppointmentDao appointmentDao;

    private BillingService billingService;

    @BeforeEach
    void setUp() throws Exception {
        billingService = new BillingService();

        // Inject mocks
        Field billDaoField = BillingService.class.getDeclaredField("billDao");
        billDaoField.setAccessible(true);
        billDaoField.set(billingService, billDao);

        Field appointmentDaoField = BillingService.class.getDeclaredField("appointmentDao");
        appointmentDaoField.setAccessible(true);
        appointmentDaoField.set(billingService, appointmentDao);
    }

    @Test
    @DisplayName("TC-BIL-001: Calculate bill for standard treatment")
    void testCalculateBillStandard() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .appointmentNumber("APT202600001")
                .patientName("John Doe")
                .dentistName("Dr. John Smith")
                .treatmentName("General Checkup")
                .treatmentCost(new BigDecimal("2000.00"))
                .consultationFee(new BigDecimal("500.00"))
                .appointmentDate(LocalDate.parse("2024-03-20"))
                .appointmentTime(LocalTime.parse("10:00:00"))
                .build();

        when(appointmentDao.findByAppointmentNumber("APT202600001")).thenReturn(Optional.of(appointment));

        // Act
        BillResponse response = billingService.calculateBill("APT202600001");

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("2000.00"), response.getTreatmentCost());
        assertEquals(new BigDecimal("500.00"), response.getConsultationFee());
        assertEquals(new BigDecimal("2500.00"), response.getTotalAmount());
        assertEquals(BigDecimal.ZERO, response.getDiscount());
    }

    @Test
    @DisplayName("TC-BIL-002: Apply 5% discount for bills over Rs. 10,000")
    void testCalculateBillWithDiscount() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .appointmentId(2)
                .appointmentNumber("APT202600002")
                .treatmentName("Root Canal Treatment")
                .treatmentCost(new BigDecimal("15000.00"))
                .consultationFee(new BigDecimal("500.00"))
                .appointmentDate(LocalDate.parse("2024-03-20"))
                .appointmentTime(LocalTime.parse("10:00:00"))
                .build();

        when(appointmentDao.findByAppointmentNumber("APT202600002")).thenReturn(Optional.of(appointment));

        // Act
        BillResponse response = billingService.calculateBill("APT202600002");

        // Assert
        // Subtotal = 15500, Discount = 775 (5%), Total = 14725
        assertEquals(new BigDecimal("15000.00"), response.getTreatmentCost());
        assertEquals(new BigDecimal("500.00"), response.getConsultationFee());
        assertEquals(new BigDecimal("775.00"), response.getDiscount());
        assertEquals(new BigDecimal("14725.00"), response.getTotalAmount());
    }

    @Test
    @DisplayName("TC-BIL-003: Generate bill for non-existent appointment")
    void testGenerateBillNonExistentAppointment() {
        // Arrange
        when(appointmentDao.findByAppointmentNumber("APT999999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            billingService.generateBill("APT999999999", 1);
        });
    }

    @Test
    @DisplayName("TC-BIL-004: Generate bill when bill already exists")
    void testGenerateBillAlreadyExists() {
        // Arrange
        Appointment appointment = Appointment.builder()
                .appointmentId(1)
                .appointmentNumber("APT202600001")
                .build();

        Bill existingBill = Bill.builder()
                .billId(1)
                .billNumber("BILL000001")
                .appointmentId(1)
                .totalAmount(new BigDecimal("2500.00"))
                .paymentStatus("PENDING")
                .build();

        when(appointmentDao.findByAppointmentNumber("APT202600001")).thenReturn(Optional.of(appointment));
        when(billDao.findByAppointmentId(1)).thenReturn(Optional.of(existingBill));

        // Act
        BillResponse response = billingService.generateBill("APT202600001", 1);

        // Assert
        assertNotNull(response);
        assertEquals("BILL000001", response.getBillNumber());

        // Verify createBill was never called
        verify(billDao, never()).createBill(any(Bill.class));
    }

    @Test
    @DisplayName("TC-BIL-005: Process payment successfully")
    void testProcessPaymentSuccess() {
        // Arrange
        Bill bill = Bill.builder()
                .billId(1)
                .billNumber("BILL000001")
                .appointmentId(1)
                .totalAmount(new BigDecimal("2500.00"))
                .paymentStatus("PENDING")
                .build();

        when(billDao.findByBillNumber("BILL000001")).thenReturn(Optional.of(bill));
        when(billDao.updatePaymentStatus(1, "PAID", "CASH")).thenReturn(true);

        // Act
        BillResponse response = billingService.processPayment("BILL000001", "CASH");

        // Assert
        assertNotNull(response);

        // Verify
        verify(billDao).updatePaymentStatus(1, "PAID", "CASH");
    }

    @Test
    @DisplayName("TC-BIL-006: Process payment with invalid method")
    void testProcessPaymentInvalidMethod() {
        // Arrange
        Bill bill = Bill.builder()
                .billId(1)
                .billNumber("BILL000001")
                .build();

        when(billDao.findByBillNumber("BILL000001")).thenReturn(Optional.of(bill));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            billingService.processPayment("BILL000001", "BITCOIN");
        }, "Should throw ValidationException for invalid payment method");
    }

    @Test
    @DisplayName("TC-BIL-007: Get bill by non-existent bill number")
    void testGetBillByNonExistentNumber() {
        // Arrange
        when(billDao.findByBillNumber("BILL999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            billingService.getBillByNumber("BILL999999");
        });
    }
}