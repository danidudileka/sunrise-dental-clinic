package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.Bill;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO class for Bill entity.
 * Handles all database operations related to billing.
 */
public class BillDao extends BaseDao {

    /**
     * Find bill by ID
     */
    public Optional<Bill> findById(int billId) {
        String sql = "SELECT b.*, a.appointment_number, p.patient_name, " +
                "d.name as dentist_name, t.treatment_name, " +
                "a.appointment_date, a.appointment_time " +
                "FROM bills b " +
                "INNER JOIN appointments a ON b.appointment_id = a.appointment_id " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE b.bill_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, billId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapBillWithDetails(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by ID: {}", billId, e);
            throw new DatabaseException("Failed to retrieve bill", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find bill by bill number
     */
    public Optional<Bill> findByBillNumber(String billNumber) {
        String sql = "SELECT b.*, a.appointment_number, p.patient_name, " +
                "d.name as dentist_name, t.treatment_name, " +
                "a.appointment_date, a.appointment_time " +
                "FROM bills b " +
                "INNER JOIN appointments a ON b.appointment_id = a.appointment_id " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE b.bill_number = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, billNumber);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapBillWithDetails(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by number: {}", billNumber, e);
            throw new DatabaseException("Failed to retrieve bill", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find bill by appointment ID
     */
    public Optional<Bill> findByAppointmentId(int appointmentId) {
        String sql = "SELECT b.*, a.appointment_number, p.patient_name, " +
                "d.name as dentist_name, t.treatment_name, " +
                "a.appointment_date, a.appointment_time " +
                "FROM bills b " +
                "INNER JOIN appointments a ON b.appointment_id = a.appointment_id " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE b.appointment_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, appointmentId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapBillWithDetails(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding bill by appointment ID: {}", appointmentId, e);
            throw new DatabaseException("Failed to retrieve bill", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create new bill
     */
    public int createBill(Bill bill) {
        String sql = "INSERT INTO bills (appointment_id, treatment_cost, consultation_fee, " +
                "additional_charges, discount, total_amount, payment_status, " +
                "payment_method, created_by, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                bill.getAppointmentId(),
                bill.getTreatmentCost(),
                bill.getConsultationFee(),
                bill.getAdditionalCharges() != null ? bill.getAdditionalCharges() : BigDecimal.ZERO,
                bill.getDiscount() != null ? bill.getDiscount() : BigDecimal.ZERO,
                bill.getTotalAmount(),
                bill.getPaymentStatus() != null ? bill.getPaymentStatus() : "PENDING",
                bill.getPaymentMethod(),
                bill.getCreatedBy(),
                bill.getNotes()
        );
    }

    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(int billId, String status, String paymentMethod) {
        String sql = "UPDATE bills SET payment_status = ?, payment_method = ? WHERE bill_id = ?";
        int affectedRows = executeUpdate(sql, status, paymentMethod, billId);
        return affectedRows > 0;
    }

    /**
     * Get bills by date range
     */
    public List<Bill> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT b.*, a.appointment_number, p.patient_name, " +
                "d.name as dentist_name, t.treatment_name, " +
                "a.appointment_date, a.appointment_time " +
                "FROM bills b " +
                "INNER JOIN appointments a ON b.appointment_id = a.appointment_id " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE DATE(b.bill_date) BETWEEN ? AND ? " +
                "ORDER BY b.bill_date DESC";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            resultSet = statement.executeQuery();

            List<Bill> bills = new ArrayList<>();
            while (resultSet.next()) {
                bills.add(mapBillWithDetails(resultSet));
            }

            return bills;

        } catch (SQLException e) {
            logger.error("Error finding bills by date range", e);
            throw new DatabaseException("Failed to retrieve bills", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get total revenue for date range
     */
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills " +
                "WHERE DATE(bill_date) BETWEEN ? AND ? AND payment_status = 'PAID'";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(startDate));
            statement.setDate(2, java.sql.Date.valueOf(endDate));

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBigDecimal(1);
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            logger.error("Error getting total revenue", e);
            throw new DatabaseException("Failed to get revenue", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Generate next bill number
     */
    public String generateNextBillNumber() {
        String sql = "SELECT CONCAT('BILL', LPAD(COALESCE(MAX(CAST(SUBSTRING(bill_number, 5) AS UNSIGNED)), 0) + 1, 6, '0')) " +
                "FROM bills";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            }

            return "BILL000001";

        } catch (SQLException e) {
            logger.error("Error generating bill number", e);
            throw new DatabaseException("Failed to generate bill number", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Map ResultSet to Bill object with joined details
     */
    private Bill mapBillWithDetails(ResultSet rs) throws SQLException {
        Bill bill = mapBill(rs);

        try {
            bill.setAppointmentNumber(rs.getString("appointment_number"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            bill.setPatientName(rs.getString("patient_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            bill.setDentistName(rs.getString("dentist_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            bill.setTreatmentName(rs.getString("treatment_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            if (rs.getDate("appointment_date") != null && rs.getTime("appointment_time") != null) {
                bill.setAppointmentDateTime(java.time.LocalDateTime.of(
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getTime("appointment_time").toLocalTime()
                ));
            }
        } catch (SQLException e) {
            // Column not in result set
        }

        return bill;
    }

    /**
     * Map ResultSet to Bill object
     */
    private Bill mapBill(ResultSet rs) throws SQLException {
        return Bill.builder()
                .billId(rs.getInt("bill_id"))
                .billNumber(rs.getString("bill_number"))
                .appointmentId(rs.getInt("appointment_id"))
                .treatmentCost(rs.getBigDecimal("treatment_cost"))
                .consultationFee(rs.getBigDecimal("consultation_fee"))
                .additionalCharges(rs.getBigDecimal("additional_charges"))
                .discount(rs.getBigDecimal("discount"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .paymentStatus(rs.getString("payment_status"))
                .paymentMethod(rs.getString("payment_method"))
                .billDate(rs.getTimestamp("bill_date") != null ?
                        rs.getTimestamp("bill_date").toLocalDateTime() : null)
                .createdBy(rs.getInt("created_by"))
                .notes(rs.getString("notes"))
                .build();
    }
}