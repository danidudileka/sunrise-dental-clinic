package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity class representing an appointment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    private int appointmentId;
    private String appointmentNumber;
    private int patientId;
    private int dentistId;
    private int treatmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private BigDecimal consultationFee;
    private String status;
    private String notes;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String patientCode;

    // Additional fields for joined queries
    private String patientName;
    private String dentistName;
    private String treatmentName;
    private BigDecimal treatmentCost;
    private String patientContactNumber;
}