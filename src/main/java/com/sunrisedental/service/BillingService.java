package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dao.BillDao;
import com.sunrisedental.dto.request.BillingRequest;
import com.sunrisedental.dto.response.BillResponse;
import com.sunrisedental.exception.NotFoundException;
import com.sunrisedental.exception.ValidationException;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.util.ValidationUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for billing operations.
 * Handles bill calculation and generation.
 */
public class BillingService {
    private static final Logger logger = LogManager.getLogger(BillingService.class);
    private final BillDao billDao;
    private final AppointmentDao appointmentDao;

    // Business rules
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.05"); // 5% discount

    public BillingService() {
        this.billDao = new BillDao();
        this.appointmentDao = new AppointmentDao();
    }

    /**
     * Generate bill for an appointment
     */
    public BillResponse generateBill(String appointmentNumber, int createdBy) {
        // Get appointment
        Optional<Appointment> appointmentOptional = appointmentDao.findByAppointmentNumber(appointmentNumber);

        if (appointmentOptional.isEmpty()) {
            throw new NotFoundException("Appointment", appointmentNumber);
        }

        Appointment appointment = appointmentOptional.get();

        // Check if bill already exists
        Optional<Bill> existingBill = billDao.findByAppointmentId(appointment.getAppointmentId());
        if (existingBill.isPresent()) {
            logger.info("Bill already exists for appointment: {}", appointmentNumber);
            return mapToBillResponse(existingBill.get());
        }

        // Calculate bill amounts
        BigDecimal treatmentCost = appointment.getTreatmentCost() != null ?
                appointment.getTreatmentCost() : BigDecimal.ZERO;
        BigDecimal consultationFee = appointment.getConsultationFee() != null ?
                appointment.getConsultationFee() : BigDecimal.ZERO;
        BigDecimal additionalCharges = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        // Calculate discount if applicable
        BigDecimal subtotal = treatmentCost.add(consultationFee);
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
            logger.debug("Applied {} discount for bill over {}", DISCOUNT_PERCENTAGE, DISCOUNT_THRESHOLD);
        }

        BigDecimal totalAmount = subtotal.subtract(discount);

        // Create bill
        Bill bill = Bill.builder()
                .appointmentId(appointment.getAppointmentId())
                .treatmentCost(treatmentCost)
                .consultationFee(consultationFee)
                .additionalCharges(additionalCharges)
                .discount(discount)
                .totalAmount(totalAmount)
                .paymentStatus("PENDING")
                .createdBy(createdBy)
                .build();

        int billId = billDao.createBill(bill);
        logger.info("Generated bill with ID: {} for appointment: {}", billId, appointmentNumber);

        // Return bill response
        Optional<Bill> createdBill = billDao.findById(billId);
        if (createdBill.isPresent()) {
            return mapToBillResponse(createdBill.get());
        }

        throw new ValidationException("Failed to generate bill");
    }

    /**
     * Calculate bill without saving (preview)
     */
    public BillResponse calculateBill(String appointmentNumber) {
        Optional<Appointment> appointmentOptional = appointmentDao.findByAppointmentNumber(appointmentNumber);

        if (appointmentOptional.isEmpty()) {
            throw new NotFoundException("Appointment", appointmentNumber);
        }

        Appointment appointment = appointmentOptional.get();

        BigDecimal treatmentCost = appointment.getTreatmentCost() != null ?
                appointment.getTreatmentCost() : BigDecimal.ZERO;
        BigDecimal consultationFee = appointment.getConsultationFee() != null ?
                appointment.getConsultationFee() : BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal subtotal = treatmentCost.add(consultationFee);
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subtotal.subtract(discount);

        return BillResponse.builder()
                .appointmentNumber(appointmentNumber)
                .patientName(appointment.getPatientName())
                .dentistName(appointment.getDentistName())
                .treatmentName(appointment.getTreatmentName())
                .treatmentCost(treatmentCost)
                .consultationFee(consultationFee)
                .additionalCharges(BigDecimal.ZERO)
                .discount(discount)
                .totalAmount(totalAmount)
                .appointmentDateTime(java.time.LocalDateTime.of(
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime()))
                .build();
    }

    /**
     * Get bill by bill number
     */
    public BillResponse getBillByNumber(String billNumber) {
        if (!ValidationUtil.isValidBillNumber(billNumber)) {
            throw new ValidationException("Invalid bill number format");
        }

        Optional<Bill> billOptional = billDao.findByBillNumber(billNumber);

        if (billOptional.isEmpty()) {
            throw new NotFoundException("Bill", billNumber);
        }

        return mapToBillResponse(billOptional.get());
    }

    /**
     * Get bill by appointment number
     */
    public BillResponse getBillByAppointmentNumber(String appointmentNumber) {
        Optional<Appointment> appointmentOptional = appointmentDao.findByAppointmentNumber(appointmentNumber);

        if (appointmentOptional.isEmpty()) {
            throw new NotFoundException("Appointment", appointmentNumber);
        }

        Optional<Bill> billOptional = billDao.findByAppointmentId(appointmentOptional.get().getAppointmentId());

        if (billOptional.isEmpty()) {
            throw new NotFoundException("Bill for appointment", appointmentNumber);
        }

        return mapToBillResponse(billOptional.get());
    }

    /**
     * Process payment for a bill
     */
    public BillResponse processPayment(String billNumber, String paymentMethod) {
        Optional<Bill> billOptional = billDao.findByBillNumber(billNumber);

        if (billOptional.isEmpty()) {
            throw new NotFoundException("Bill", billNumber);
        }

        // Validate payment method
        if (!isValidPaymentMethod(paymentMethod)) {
            throw new ValidationException("Invalid payment method");
        }

        Bill bill = billOptional.get();
        boolean updated = billDao.updatePaymentStatus(bill.getBillId(), "PAID", paymentMethod);

        if (updated) {
            logger.info("Payment processed for bill: {}", billNumber);
            return getBillByNumber(billNumber);
        }

        throw new ValidationException("Failed to process payment");
    }

    /**
     * Get bills by date range
     */
    public List<BillResponse> getBillsByDateRange(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr, "start date");
        LocalDate endDate = parseDate(endDateStr, "end date");

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        List<Bill> bills = billDao.findByDateRange(startDate, endDate);
        return bills.stream()
                .map(this::mapToBillResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get total revenue for date range
     */
    public BigDecimal getTotalRevenue(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr, "start date");
        LocalDate endDate = parseDate(endDateStr, "end date");

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        return billDao.getTotalRevenue(startDate, endDate);
    }

    /**
     * Parse date with validation
     */
    private LocalDate parseDate(String dateStr, String fieldName) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new ValidationException("Invalid " + fieldName + " format");
        }
    }

    /**
     * Check if payment method is valid
     */
    private boolean isValidPaymentMethod(String method) {
        return method != null && (
                method.equals("CASH") ||
                        method.equals("CARD") ||
                        method.equals("INSURANCE") ||
                        method.equals("ONLINE")
        );
    }

    /**
     * Map Bill to BillResponse
     */
    private BillResponse mapToBillResponse(Bill bill) {
        return BillResponse.builder()
                .billId(bill.getBillId())
                .billNumber(bill.getBillNumber())
                .appointmentNumber(bill.getAppointmentNumber())
                .patientName(bill.getPatientName())
                .dentistName(bill.getDentistName())
                .treatmentName(bill.getTreatmentName())
                .treatmentCost(bill.getTreatmentCost())
                .consultationFee(bill.getConsultationFee())
                .additionalCharges(bill.getAdditionalCharges())
                .discount(bill.getDiscount())
                .totalAmount(bill.getTotalAmount())
                .paymentStatus(bill.getPaymentStatus())
                .paymentMethod(bill.getPaymentMethod())
                .billDate(bill.getBillDate())
                .appointmentDateTime(bill.getAppointmentDateTime())
                .notes(bill.getNotes())
                .build();
    }
}