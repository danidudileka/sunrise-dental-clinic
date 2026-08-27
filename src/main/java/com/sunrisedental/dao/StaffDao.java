package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO class for Staff entity.
 * Handles staff-dentist mappings.
 */
public class StaffDao extends BaseDao {

    /**
     * Find staff by user ID
     */
    public Optional<Staff> findByUserId(int userId) {
        String sql = "SELECT * FROM staff WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapStaff(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding staff by user ID: {}", userId, e);
            throw new DatabaseException("Failed to retrieve staff", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create staff record
     */
    public int createStaff(Staff staff) {
        String sql = "INSERT INTO staff (user_id, staff_type, dentist_id) VALUES (?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                staff.getUserId(),
                staff.getStaffType(),
                staff.getDentistId()
        );
    }

    /**
     * Map ResultSet to Staff object
     */
    private Staff mapStaff(ResultSet rs) throws SQLException {
        return Staff.builder()
                .staffId(rs.getInt("staff_id"))
                .userId(rs.getInt("user_id"))
                .staffType(rs.getString("staff_type"))
                .dentistId(rs.getInt("dentist_id") != 0 ? rs.getInt("dentist_id") : null)
                .createdAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build();
    }
}