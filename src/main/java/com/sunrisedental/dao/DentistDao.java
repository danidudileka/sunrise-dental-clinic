package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.Dentist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO class for Dentist entity.
 * Handles all database operations related to dentists.
 */
public class DentistDao extends BaseDao {

    /**
     * Find dentist by ID
     */
    public Optional<Dentist> findById(int dentistId) {
        String sql = "SELECT * FROM dentists WHERE dentist_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, dentistId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapDentist(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding dentist by ID: {}", dentistId, e);
            throw new DatabaseException("Failed to retrieve dentist", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find dentist by name
     */
    public Optional<Dentist> findByName(String name) {
        String sql = "SELECT * FROM dentists WHERE name = ? AND is_active = true";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, name);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapDentist(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding dentist by name: {}", name, e);
            throw new DatabaseException("Failed to retrieve dentist", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get all active dentists
     */
    public List<Dentist> findAllActive() {
        String sql = "SELECT * FROM dentists WHERE is_active = true ORDER BY name";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            List<Dentist> dentists = new ArrayList<>();
            while (resultSet.next()) {
                dentists.add(mapDentist(resultSet));
            }

            return dentists;

        } catch (SQLException e) {
            logger.error("Error finding all active dentists", e);
            throw new DatabaseException("Failed to retrieve dentists", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create new dentist
     */
    public int createDentist(Dentist dentist) {
        String sql = "INSERT INTO dentists (name, specialization, license_number, phone, email) " +
                "VALUES (?, ?, ?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                dentist.getName(),
                dentist.getSpecialization(),
                dentist.getLicenseNumber(),
                dentist.getPhone(),
                dentist.getEmail()
        );
    }

    /**
     * Update dentist
     */
    public boolean updateDentist(Dentist dentist) {
        String sql = "UPDATE dentists SET " +
                "name = ?, specialization = ?, phone = ?, email = ? " +
                "WHERE dentist_id = ?";

        int affectedRows = executeUpdate(sql,
                dentist.getName(),
                dentist.getSpecialization(),
                dentist.getPhone(),
                dentist.getEmail(),
                dentist.getDentistId()
        );

        return affectedRows > 0;
    }

    /**
     * Map ResultSet to Dentist object
     */
    private Dentist mapDentist(ResultSet rs) throws SQLException {
        return Dentist.builder()
                .dentistId(rs.getInt("dentist_id"))
                .name(rs.getString("name"))
                .specialization(rs.getString("specialization"))
                .licenseNumber(rs.getString("license_number"))
                .phone(rs.getString("phone"))
                .email(rs.getString("email"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build();
    }
}