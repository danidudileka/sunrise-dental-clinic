package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a treatment type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Treatment {
    private int treatmentId;
    private String treatmentCode;
    private String treatmentName;
    private String description;
    private BigDecimal baseCost;
    private int durationMinutes;
    private boolean isActive;
    private LocalDateTime createdAt;
}