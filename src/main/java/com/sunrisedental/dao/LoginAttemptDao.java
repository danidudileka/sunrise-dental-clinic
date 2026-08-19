package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.LoginAttempt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * DAO class for LoginAttempt entity.
 * Tracks login attempts for security purposes.
 */
public class LoginAttemptDao extends BaseDao {

    /**
     * Record a login attempt
     */
    public void recordAttempt(String username, String ipAddress, boolean success) {
        String sql = "INSERT INTO login_attempts (username, attempt_time, ip_address, success) " +
                "VALUES (?, ?, ?, ?)";

        executeUpdate(sql, username, LocalDateTime.now(), ipAddress, success);
    }

    /**
     * Get recent failed login attempts for a user
     */
    public int getRecentFailedAttempts(String username, int minutes) {
        String sql = "SELECT COUNT(*) FROM login_attempts " +
                "WHERE username = ? AND success = false " +
                "AND attempt_time >= DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setInt(2, minutes);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error getting recent failed attempts for username: {}", username, e);
            throw new DatabaseException("Failed to retrieve login attempts", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Get last login attempt time
     */
    public LocalDateTime getLastLoginAttemptTime(String username) {
        String sql = "SELECT MAX(attempt_time) FROM login_attempts WHERE username = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();

            if (resultSet.next() && resultSet.getTimestamp(1) != null) {
                return resultSet.getTimestamp(1).toLocalDateTime();
            }

            return null;

        } catch (SQLException e) {
            logger.error("Error getting last login attempt for username: {}", username, e);
            throw new DatabaseException("Failed to retrieve login attempt time", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Clear login attempts for a user
     */
    public void clearAttempts(String username) {
        String sql = "DELETE FROM login_attempts WHERE username = ?";
        executeUpdate(sql, username);
    }
}