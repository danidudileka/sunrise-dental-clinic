package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.Treatment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO class for Treatment entity.
 * Handles all database operations related to treatments.
 */
public class TreatmentDao extends BaseDao {

    /**
     * Find treatment by ID
     */
    public Optional<Treatment> findById(int treatmentId) {
        String sql = "SELECT * FROM treatments WHERE treatment_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, treatmentId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapTreatment(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding treatment by ID: {}", treatmentId, e);
            throw new DatabaseException("Failed to retrieve treatment", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find treatment by name
     */
    public Optional<Treatment> findByName(String treatmentName) {
        String sql = "SELECT * FROM treatments WHERE treatment_name = ? AND is_active = true";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, treatmentName);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapTreatment(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding treatment by name: {}", treatmentName, e);
            throw new DatabaseException("Failed to retrieve treatment", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find treatment by code
     */
    public Optional<Treatment> findByCode(String treatmentCode) {
        String sql = "SELECT * FROM treatments WHERE treatment_code = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, treatmentCode);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapTreatment(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding treatment by code: {}", treatmentCode, e);
            throw new DatabaseException("Failed to retrieve treatment", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get all active treatments
     */
    public List<Treatment> findAllActive() {
        String sql = "SELECT * FROM treatments WHERE is_active = true ORDER BY treatment_name";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            List<Treatment> treatments = new ArrayList<>();
            while (resultSet.next()) {
                treatments.add(mapTreatment(resultSet));
            }

            return treatments;

        } catch (SQLException e) {
            logger.error("Error finding all active treatments", e);
            throw new DatabaseException("Failed to retrieve treatments", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create new treatment
     */
    public int createTreatment(Treatment treatment) {
        String sql = "INSERT INTO treatments (treatment_code, treatment_name, description, " +
                "base_cost, duration_minutes) VALUES (?, ?, ?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                treatment.getTreatmentCode(),
                treatment.getTreatmentName(),
                treatment.getDescription(),
                treatment.getBaseCost(),
                treatment.getDurationMinutes()
        );
    }

    /**
     * Update treatment
     */
    public boolean updateTreatment(Treatment treatment) {
        String sql = "UPDATE treatments SET " +
                "treatment_name = ?, description = ?, base_cost = ?, duration_minutes = ? " +
                "WHERE treatment_id = ?";

        int affectedRows = executeUpdate(sql,
                treatment.getTreatmentName(),
                treatment.getDescription(),
                treatment.getBaseCost(),
                treatment.getDurationMinutes(),
                treatment.getTreatmentId()
        );

        return affectedRows > 0;
    }

    /**
     * Map ResultSet to Treatment object
     */
    private Treatment mapTreatment(ResultSet rs) throws SQLException {
        return Treatment.builder()
                .treatmentId(rs.getInt("treatment_id"))
                .treatmentCode(rs.getString("treatment_code"))
                .treatmentName(rs.getString("treatment_name"))
                .description(rs.getString("description"))
                .baseCost(rs.getBigDecimal("base_cost"))
                .durationMinutes(rs.getInt("duration_minutes"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build();
    }
}