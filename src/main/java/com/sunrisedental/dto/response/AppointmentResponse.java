package com.sunrisedental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for appointment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private int appointmentId;
    private String appointmentNumber;
    private String patientName;
    private String address;
    private String contactNumber;
    private String email;
    private String dentistName;
    private String treatmentType;
    private BigDecimal treatmentCost;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private BigDecimal consultationFee;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}