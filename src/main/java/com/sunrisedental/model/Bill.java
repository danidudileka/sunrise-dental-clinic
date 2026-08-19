package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a bill.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    private int billId;
    private String billNumber;
    private int appointmentId;
    private BigDecimal treatmentCost;
    private BigDecimal consultationFee;
    private BigDecimal additionalCharges;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime billDate;
    private int createdBy;
    private String notes;

    // Additional fields for joined queries
    private String appointmentNumber;
    private String patientName;
    private String treatmentName;
    private String dentistName;
    private LocalDateTime appointmentDateTime;
}