package com.sunrisedental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for patient response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private int patientId;
    private String patientName;
    private String address;
    private String contactNumber;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String medicalHistory;
    private LocalDateTime createdAt;
    private int totalAppointments;
    private LocalDate lastVisitDate;
}