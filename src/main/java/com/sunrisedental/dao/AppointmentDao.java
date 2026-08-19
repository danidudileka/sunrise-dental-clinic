package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO class for Appointment entity.
 * Handles all database operations related to appointments.
 */
public class AppointmentDao extends BaseDao {

    /**
     * Find appointment by ID
     */
    public Optional<Appointment> findById(int appointmentId) {
        String sql = "SELECT a.*, p.patient_name, p.contact_number as patient_contact, " +
                "d.name as dentist_name, t.treatment_name, t.base_cost as treatment_cost " +
                "FROM appointments a " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, appointmentId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapAppointmentWithDetails(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding appointment by ID: {}", appointmentId, e);
            throw new DatabaseException("Failed to retrieve appointment", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find appointment by appointment number
     */
    public Optional<Appointment> findByAppointmentNumber(String appointmentNumber) {
        String sql = "SELECT a.*, p.patient_name, p.contact_number as patient_contact, " +
                "p.address, p.email, d.name as dentist_name, " +
                "t.treatment_name, t.base_cost as treatment_cost " +
                "FROM appointments a " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_number = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, appointmentNumber);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapAppointmentWithDetails(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding appointment by number: {}", appointmentNumber, e);
            throw new DatabaseException("Failed to retrieve appointment", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Check if appointment number exists
     */
    public boolean existsByAppointmentNumber(String appointmentNumber) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_number = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, appointmentNumber);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking appointment number existence: {}", appointmentNumber, e);
            throw new DatabaseException("Failed to check appointment", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get appointments by date
     */
    public List<Appointment> findByDate(LocalDate date) {
        String sql = "SELECT a.*, p.patient_name, p.contact_number as patient_contact, " +
                "d.name as dentist_name, t.treatment_name, t.base_cost as treatment_cost " +
                "FROM appointments a " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.appointment_date = ? " +
                "ORDER BY a.appointment_time";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setDate(1, java.sql.Date.valueOf(date));

            resultSet = statement.executeQuery();

            List<Appointment> appointments = new ArrayList<>();
            while (resultSet.next()) {
                appointments.add(mapAppointmentWithDetails(resultSet));
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by date: {}", date, e);
            throw new DatabaseException("Failed to retrieve appointments", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get appointments by patient ID
     */
    public List<Appointment> findByPatientId(int patientId) {
        String sql = "SELECT a.*, p.patient_name, p.contact_number as patient_contact, " +
                "d.name as dentist_name, t.treatment_name, t.base_cost as treatment_cost " +
                "FROM appointments a " +
                "INNER JOIN patients p ON a.patient_id = p.patient_id " +
                "INNER JOIN dentists d ON a.dentist_id = d.dentist_id " +
                "INNER JOIN treatments t ON a.treatment_id = t.treatment_id " +
                "WHERE a.patient_id = ? " +
                "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, patientId);

            resultSet = statement.executeQuery();

            List<Appointment> appointments = new ArrayList<>();
            while (resultSet.next()) {
                appointments.add(mapAppointmentWithDetails(resultSet));
            }

            return appointments;

        } catch (SQLException e) {
            logger.error("Error finding appointments by patient ID: {}", patientId, e);
            throw new DatabaseException("Failed to retrieve appointments", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Check for double booking (same dentist, date, and time)
     */
    public boolean isDoubleBooked(int dentistId, LocalDate date, java.time.LocalTime time) {
        String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ? " +
                "AND status = 'SCHEDULED'";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, dentistId);
            statement.setDate(2, java.sql.Date.valueOf(date));
            statement.setTime(3, java.sql.Time.valueOf(time));

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking double booking", e);
            throw new DatabaseException("Failed to check booking", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create new appointment
     */
    public int createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (appointment_number, patient_id, dentist_id, " +
                "treatment_id, appointment_date, appointment_time, consultation_fee, " +
                "status, notes, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                appointment.getAppointmentNumber(),
                appointment.getPatientId(),
                appointment.getDentistId(),
                appointment.getTreatmentId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getConsultationFee(),
                appointment.getStatus() != null ? appointment.getStatus() : "SCHEDULED",
                appointment.getNotes(),
                appointment.getCreatedBy()
        );
    }

    /**
     * Update appointment status
     */
    public boolean updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        int affectedRows = executeUpdate(sql, status, appointmentId);
        return affectedRows > 0;
    }

    /**
     * Update appointment
     */
    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE appointments SET " +
                "dentist_id = ?, treatment_id = ?, appointment_date = ?, " +
                "appointment_time = ?, consultation_fee = ?, notes = ? " +
                "WHERE appointment_id = ?";

        int affectedRows = executeUpdate(sql,
                appointment.getDentistId(),
                appointment.getTreatmentId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getConsultationFee(),
                appointment.getNotes(),
                appointment.getAppointmentId()
        );

        return affectedRows > 0;
    }

    /**
     * Get appointment count for a date range
     */
    public int getAppointmentCount(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE appointment_date BETWEEN ? AND ?";

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
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error getting appointment count", e);
            throw new DatabaseException("Failed to get appointment count", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Generate next appointment number
     */
    public String generateNextAppointmentNumber() {
        String sql = "SELECT CONCAT('APT', LPAD(COALESCE(MAX(CAST(SUBSTRING(appointment_number, 4) AS UNSIGNED)), 0) + 1, 9, '0')) " +
                "FROM appointments";

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

            return "APT000000001";

        } catch (SQLException e) {
            logger.error("Error generating appointment number", e);
            throw new DatabaseException("Failed to generate appointment number", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Map ResultSet to Appointment object with joined details
     */
    private Appointment mapAppointmentWithDetails(ResultSet rs) throws SQLException {
        Appointment appointment = mapAppointment(rs);

        // Set joined fields
        try {
            appointment.setPatientName(rs.getString("patient_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            appointment.setDentistName(rs.getString("dentist_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            appointment.setTreatmentName(rs.getString("treatment_name"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            appointment.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        } catch (SQLException e) {
            // Column not in result set
        }

        try {
            appointment.setPatientContactNumber(rs.getString("patient_contact"));
        } catch (SQLException e) {
            // Column not in result set
        }

        return appointment;
    }

    /**
     * Map ResultSet to Appointment object
     */
    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        return Appointment.builder()
                .appointmentId(rs.getInt("appointment_id"))
                .appointmentNumber(rs.getString("appointment_number"))
                .patientId(rs.getInt("patient_id"))
                .dentistId(rs.getInt("dentist_id"))
                .treatmentId(rs.getInt("treatment_id"))
                .appointmentDate(rs.getDate("appointment_date") != null ?
                        rs.getDate("appointment_date").toLocalDate() : null)
                .appointmentTime(rs.getTime("appointment_time") != null ?
                        rs.getTime("appointment_time").toLocalTime() : null)
                .consultationFee(rs.getBigDecimal("consultation_fee"))
                .status(rs.getString("status"))
                .notes(rs.getString("notes"))
                .createdBy(rs.getInt("created_by"))
                .createdAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ?
                        rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}