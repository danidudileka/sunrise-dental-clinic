package com.sunrisedental.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for billing requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequest {
    private String appointmentNumber;
    private String billNumber;
    private BigDecimal additionalCharges;
    private BigDecimal discount;
    private String paymentMethod;
    private String notes;
}