package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dao.BillDao;
import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.dao.ReportDao;
import com.sunrisedental.dto.response.ReportResponse;
import com.sunrisedental.exception.ValidationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for report generation.
 * Provides various analytical reports for management.
 */
public class ReportService {
    private static final Logger logger = LogManager.getLogger(ReportService.class);
    private final ReportDao reportDao;
    private final AppointmentDao appointmentDao;
    private final BillDao billDao;
    private final PatientDao patientDao;

    public ReportService() {
        this.reportDao = new ReportDao();
        this.appointmentDao = new AppointmentDao();
        this.billDao = new BillDao();
        this.patientDao = new PatientDao();
    }

    /**
     * Generate comprehensive report for date range
     */
    public ReportResponse generateReport(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr, "start date");
        LocalDate endDate = parseDate(endDateStr, "end date");

        validateDateRange(startDate, endDate);

        ReportResponse report = reportDao.getReportSummary(startDate, endDate);

        // Get daily reports
        List<ReportResponse.DailyReport> dailyReports = generateDailyReports(startDate, endDate);
        report.setDailyReports(dailyReports);

        // Calculate average revenue
        if (!dailyReports.isEmpty()) {
            BigDecimal totalRevenue = dailyReports.stream()
                    .map(ReportResponse.DailyReport::getTotalRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgRevenue = totalRevenue.divide(
                    BigDecimal.valueOf(dailyReports.size()), 2, java.math.RoundingMode.HALF_UP);

            report.setTotalRevenue(totalRevenue);
            report.setAverageRevenuePerDay(avgRevenue);
        }

        logger.info("Generated report from {} to {}", startDate, endDate);
        return report;
    }

    /**
     * Generate daily appointment report
     */
    public List<Map<String, Object>> getDailyAppointmentsReport(String dateStr) {
        LocalDate date = parseDate(dateStr, "date");
        return reportDao.getDailyAppointments(date);
    }

    /**
     * Generate revenue report
     */
    public List<Map<String, Object>> getRevenueReport(String startDateStr, String endDateStr) {
        LocalDate startDate = parseDate(startDateStr, "start date");
        LocalDate endDate = parseDate(endDateStr, "end date");

        validateDateRange(startDate, endDate);

        return reportDao.getRevenueReport(startDate, endDate);
    }

    /**
     * Generate dashboard summary
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusMonths(1);

        // Today's appointments
        int todayAppointments = appointmentDao.getAppointmentCount(today, today);
        summary.put("todayAppointments", todayAppointments);

        // This week's appointments
        int weekAppointments = appointmentDao.getAppointmentCount(weekStart, today);
        summary.put("weekAppointments", weekAppointments);

        // Total patients
        int totalPatients = patientDao.getTotalPatientCount();
        summary.put("totalPatients", totalPatients);

        // This month's revenue
        BigDecimal monthRevenue = billDao.getTotalRevenue(monthStart, today);
        summary.put("monthRevenue", monthRevenue);

        // Today's revenue
        BigDecimal todayRevenue = billDao.getTotalRevenue(today, today);
        summary.put("todayRevenue", todayRevenue);

        return summary;
    }

    /**
     * Generate daily reports for date range
     */
    private List<ReportResponse.DailyReport> generateDailyReports(LocalDate startDate, LocalDate endDate) {
        List<ReportResponse.DailyReport> dailyReports = new java.util.ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            ReportResponse.DailyReport dailyReport = ReportResponse.DailyReport.builder()
                    .date(currentDate)
                    .totalAppointments(appointmentDao.getAppointmentCount(currentDate, currentDate))
                    .totalRevenue(billDao.getTotalRevenue(currentDate, currentDate))
                    .build();

            dailyReports.add(dailyReport);
            currentDate = currentDate.plusDays(1);
        }

        return dailyReports;
    }

    /**
     * Parse date with validation
     */
    private LocalDate parseDate(String dateStr, String fieldName) {
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                throw new ValidationException(fieldName + " is required");
            }
            return LocalDate.parse(dateStr);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidationException("Invalid " + fieldName + " format");
        }
    }

    /**
     * Validate date range
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }

        // Limit range to 1 year
        if (startDate.plusYears(1).isBefore(endDate)) {
            throw new ValidationException("Date range cannot exceed 1 year");
        }
    }
}