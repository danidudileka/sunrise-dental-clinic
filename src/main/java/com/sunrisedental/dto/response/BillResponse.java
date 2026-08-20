package com.sunrisedental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for bill response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private int billId;
    private String billNumber;
    private String appointmentNumber;
    private String patientName;
    private String dentistName;
    private String treatmentName;
    private BigDecimal treatmentCost;
    private BigDecimal consultationFee;
    private BigDecimal additionalCharges;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime billDate;
    private LocalDateTime appointmentDateTime;
    private String notes;
}