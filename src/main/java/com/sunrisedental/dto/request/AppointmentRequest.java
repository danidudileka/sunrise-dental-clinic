package com.sunrisedental.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for appointment creation/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    private String appointmentNumber;
    private String patientName;
    private String address;
    private String contactNumber;
    private String email;
    private String dentistName;
    private String treatmentType;
    private String appointmentDate;
    private String appointmentTime;
    private BigDecimal consultationFee;
    private String notes;

    // Optional: if patient already exists
    private Integer patientId;
    private Integer dentistId;
    private Integer treatmentId;
}