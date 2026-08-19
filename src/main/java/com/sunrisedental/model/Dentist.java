package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a dentist.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dentist {
    private int dentistId;
    private String name;
    private String specialization;
    private String licenseNumber;
    private String phone;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;
}