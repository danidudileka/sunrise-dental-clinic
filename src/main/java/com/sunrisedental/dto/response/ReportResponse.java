package com.sunrisedental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for report responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenuePerDay;
    private Map<String, Integer> appointmentsByStatus;
    private Map<String, Integer> appointmentsByDentist;
    private Map<String, BigDecimal> revenueByTreatment;
    private List<DailyReport> dailyReports;

    /**
     * Inner class for daily report data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyReport {
        private LocalDate date;
        private int totalAppointments;
        private int totalBills;
        private BigDecimal totalRevenue;
    }
}