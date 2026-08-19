package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.dto.response.ReportResponse;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class for generating reports.
 * Uses stored procedures and complex queries for analytics.
 */
public class ReportDao extends BaseDao {

    /**
     * Get daily appointments report using stored procedure
     */
    public List<Map<String, Object>> getDailyAppointments(LocalDate date) {
        String sql = "{CALL GetDailyAppointments(?)}";
        List<Map<String, Object>> results = new ArrayList<>();

        Connection connection = null;
        CallableStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareCall(sql);
            statement.setDate(1, java.sql.Date.valueOf(date));

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("appointmentNumber", resultSet.getString("appointment_number"));
                row.put("appointmentTime", resultSet.getTime("appointment_time").toString());
                row.put("patientName", resultSet.getString("patient_name"));
                row.put("contactNumber", resultSet.getString("contact_number"));
                row.put("dentistName", resultSet.getString("dentist_name"));
                row.put("treatmentName", resultSet.getString("treatment_name"));
                row.put("status", resultSet.getString("status"));
                results.add(row);
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting daily appointments report", e);
            throw new DatabaseException("Failed to generate daily report", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get revenue report using stored procedure
     */
    public List<Map<String, Object>> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        String sql = "{CALL CalculateRevenue(?, ?)}";
        List<Map<String, Object>> results = new ArrayList<>();

        Connection connection = null;
        CallableStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareCall(sql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("billDate", resultSet.getDate("bill_date").toLocalDate().toString());
                row.put("totalBills", resultSet.getInt("total_bills"));
                row.put("totalRevenue", resultSet.getBigDecimal("total_revenue"));
                row.put("treatmentRevenue", resultSet.getBigDecimal("treatment_revenue"));
                row.put("consultationRevenue", resultSet.getBigDecimal("consultation_revenue"));
                results.add(row);
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting revenue report", e);
            throw new DatabaseException("Failed to generate revenue report", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get appointment statistics by status
     */
    public Map<String, Integer> getAppointmentsByStatus(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT status, COUNT(*) as count FROM appointments " +
                "WHERE appointment_date BETWEEN ? AND ? " +
                "GROUP BY status";

        Map<String, Integer> results = new HashMap<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                results.put(resultSet.getString("status"), resultSet.getInt("count"));
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting appointments by status", e);
            throw new DatabaseException("Failed to get appointment statistics", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get appointments by dentist
     */
    public Map<String, Integer> getAppointmentsByDentist(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT d.name, COUNT(a.appointment_id) as count " +
                "FROM dentists d " +
                "LEFT JOIN appointments a ON d.dentist_id = a.dentist_id " +
                "AND a.appointment_date BETWEEN ? AND ? " +
                "GROUP BY d.dentist_id, d.name " +
                "ORDER BY count DESC";

        Map<String, Integer> results = new HashMap<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                results.put(resultSet.getString("name"), resultSet.getInt("count"));
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting appointments by dentist", e);
            throw new DatabaseException("Failed to get dentist statistics", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get revenue by treatment
     */
    public Map<String, BigDecimal> getRevenueByTreatment(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT t.treatment_name, COALESCE(SUM(b.total_amount), 0) as revenue " +
                "FROM treatments t " +
                "LEFT JOIN appointments a ON t.treatment_id = a.treatment_id " +
                "LEFT JOIN bills b ON a.appointment_id = b.appointment_id " +
                "AND b.payment_status = 'PAID' " +
                "AND DATE(b.bill_date) BETWEEN ? AND ? " +
                "GROUP BY t.treatment_id, t.treatment_name " +
                "ORDER BY revenue DESC";

        Map<String, BigDecimal> results = new HashMap<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                results.put(resultSet.getString("treatment_name"), resultSet.getBigDecimal("revenue"));
            }

            return results;

        } catch (SQLException e) {
            logger.error("Error getting revenue by treatment", e);
            throw new DatabaseException("Failed to get treatment revenue", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get complete report summary
     */
    public ReportResponse getReportSummary(LocalDate startDate, LocalDate endDate) {
        ReportResponse report = new ReportResponse();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Get total appointments
        String countSql = "SELECT COUNT(*) FROM appointments WHERE appointment_date BETWEEN ? AND ?";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(countSql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                report.setTotalAppointments(resultSet.getInt(1));
            }

        } catch (SQLException e) {
            logger.error("Error getting total appointments for report", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }

        // Set other report data
        report.setAppointmentsByStatus(getAppointmentsByStatus(startDate, endDate));
        report.setAppointmentsByDentist(getAppointmentsByDentist(startDate, endDate));
        report.setRevenueByTreatment(getRevenueByTreatment(startDate, endDate));

        return report;
    }
}