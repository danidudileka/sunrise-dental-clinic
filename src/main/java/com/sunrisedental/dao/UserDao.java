package com.sunrisedental.dao;

import com.sunrisedental.exception.DatabaseException;
import com.sunrisedental.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * DAO class for User entity.
 * Handles all database operations related to users.
 */
public class UserDao extends BaseDao {

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapUser(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new DatabaseException("Failed to retrieve user", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapUser(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by ID: {}", userId, e);
            throw new DatabaseException("Failed to retrieve user", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Create new user
     */
    public int createUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role) " +
                "VALUES (?, ?, ?, ?, ?)";

        return executeUpdateWithGeneratedKey(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Update user
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET " +
                "full_name = ?, email = ?, role = ?, is_active = ? " +
                "WHERE user_id = ?";

        int affectedRows = executeUpdate(sql,
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getUserId()
        );

        return affectedRows > 0;
    }

    /**
     * Update password
     */
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        int affectedRows = executeUpdate(sql, newPasswordHash, userId);

        return affectedRows > 0;
    }

    /**
     * Delete user (soft delete)
     */
    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET is_active = false WHERE user_id = ?";

        int affectedRows = executeUpdate(sql, userId);

        return affectedRows > 0;
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking username existence: {}", username, e);
            throw new DatabaseException("Failed to check username", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }

    /**
     * Map ResultSet to User object
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return User.builder()
                .userId(rs.getInt("user_id"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .fullName(rs.getString("full_name"))
                .email(rs.getString("email"))
                .role(rs.getString("role"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ?
                        rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}