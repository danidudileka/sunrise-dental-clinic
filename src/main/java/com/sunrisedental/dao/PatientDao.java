package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO class for Patient entity.
 * Handles all database operations related to patients.
 */
public class PatientDao extends BaseDao {

    /**
     * Find patient by ID
     */
    public Optional<Patient> findById(int patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, patientId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapPatient(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding patient by ID: {}", patientId, e);
            throw new DatabaseException("Failed to retrieve patient", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find patient by contact number
     */
    public Optional<Patient> findByContactNumber(String contactNumber) {
        String sql = "SELECT * FROM patients WHERE contact_number = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, contactNumber);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapPatient(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding patient by contact number: {}", contactNumber, e);
            throw new DatabaseException("Failed to retrieve patient", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find patient by name (partial match)
     */
    public List<Patient> findByName(String name) {
        String sql = "SELECT * FROM patients WHERE patient_name LIKE ? AND is_active = true";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + name + "%");

            resultSet = statement.executeQuery();

            List<Patient> patients = new ArrayList<>();
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding patients by name: {}", name, e);
            throw new DatabaseException("Failed to retrieve patients", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get all active patients
     */
    public List<Patient> findAllActive() {
        String sql = "SELECT * FROM patients WHERE is_active = true ORDER BY patient_name";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            List<Patient> patients = new ArrayList<>();
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }

            return patients;

        } catch (SQLException e) {
            logger.error("Error finding all active patients", e);
            throw new DatabaseException("Failed to retrieve patients", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create new patient
     */
    public int createPatient(Patient patient) {
        String sql = "INSERT INTO patients (patient_name, address, contact_number, email, " +
                "date_of_birth, gender, blood_group, medical_history) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                patient.getPatientName(),
                patient.getAddress(),
                patient.getContactNumber(),
                patient.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getMedicalHistory()
        );
    }

    /**
     * Update patient
     */
    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET " +
                "patient_name = ?, address = ?, contact_number = ?, email = ?, " +
                "date_of_birth = ?, gender = ?, blood_group = ?, medical_history = ? " +
                "WHERE patient_id = ?";

        int affectedRows = executeUpdate(sql,
                patient.getPatientName(),
                patient.getAddress(),
                patient.getContactNumber(),
                patient.getEmail(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getMedicalHistory(),
                patient.getPatientId()
        );

        return affectedRows > 0;
    }

    /**
     * Deactivate patient (soft delete)
     */
    public boolean deactivatePatient(int patientId) {
        String sql = "UPDATE patients SET is_active = false WHERE patient_id = ?";
        int affectedRows = executeUpdate(sql, patientId);
        return affectedRows > 0;
    }

    /**
     * Check if patient exists by contact number
     */
    public boolean existsByContactNumber(String contactNumber) {
        String sql = "SELECT COUNT(*) FROM patients WHERE contact_number = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, contactNumber);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking patient existence by contact: {}", contactNumber, e);
            throw new DatabaseException("Failed to check patient existence", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get total patient count
     */
    public int getTotalPatientCount() {
        String sql = "SELECT COUNT(*) FROM patients WHERE is_active = true";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error getting total patient count", e);
            throw new DatabaseException("Failed to get patient count", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Map ResultSet to Patient object
     */
    private Patient mapPatient(ResultSet rs) throws SQLException {
        return Patient.builder()
                .patientId(rs.getInt("patient_id"))
                .patientName(rs.getString("patient_name"))
                .address(rs.getString("address"))
                .contactNumber(rs.getString("contact_number"))
                .email(rs.getString("email"))
                .dateOfBirth(rs.getDate("date_of_birth") != null ?
                        rs.getDate("date_of_birth").toLocalDate() : null)
                .gender(rs.getString("gender"))
                .bloodGroup(rs.getString("blood_group"))
                .medicalHistory(rs.getString("medical_history"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ?
                        rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}