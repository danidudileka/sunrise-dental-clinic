package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a patient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private int patientId;
    private String patientName;
    private String address;
    private String contactNumber;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String medicalHistory;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}