package com.sunrisedental.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for patient registration/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {
    private String patientName;
    private String address;
    private String contactNumber;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String medicalHistory;
}