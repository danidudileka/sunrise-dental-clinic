package com.sunrisedental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {
    private int staffId;
    private int userId;
    private String staffType;  // DENTIST or RECEPTIONIST
    private Integer dentistId;  // Null for receptionists
    private LocalDateTime createdAt;
}