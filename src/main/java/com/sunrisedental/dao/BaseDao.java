package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.exception.DatabaseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base DAO class with common database operations.
 * All DAO classes should extend this class.
 */
public abstract class BaseDao {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final DatabaseConfig dbConfig;

    public BaseDao() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Get database connection
     */
    protected Connection getConnection() throws SQLException {
        return dbConfig.getConnection();
    }

    /**
     * Close database resources
     */
    protected void closeResources(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.warn("Error closing ResultSet", e);
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.warn("Error closing Statement", e);
            }
        }

        if (connection != null) {
            dbConfig.closeConnection(connection);
        }
    }

    /**
     * Close database resources without ResultSet
     */
    protected void closeResources(Connection connection, Statement statement) {
        closeResources(connection, statement, null);
    }

    /**
     * Execute update query and return generated key
     */
    protected int executeUpdateWithGeneratedKey(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            setParameters(statement, params);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new DatabaseException("No rows affected");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DatabaseException("No generated key returned");
            }

        } catch (SQLException e) {
            logger.error("Error executing update with generated key", e);
            throw new DatabaseException("Database operation failed", e);
        } finally {
            closeResources(connection, statement, generatedKeys);
        }
    }

    /**
     * Execute update query
     */
    protected int executeUpdate(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);

            setParameters(statement, params);

            return statement.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error executing update", e);
            throw new DatabaseException("Database operation failed", e);
        } finally {
            closeResources(connection, statement);
        }
    }

    /**
     * Execute query and return ResultSet
     */
    protected ResultSet executeQuery(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, params);
        return statement.executeQuery();
    }

    /**
     * Set parameters for PreparedStatement
     */
    protected void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];

                if (param instanceof String) {
                    statement.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof Long) {
                    statement.setLong(i + 1, (Long) param);
                } else if (param instanceof Double) {
                    statement.setDouble(i + 1, (Double) param);
                } else if (param instanceof Float) {
                    statement.setFloat(i + 1, (Float) param);
                } else if (param instanceof Boolean) {
                    statement.setBoolean(i + 1, (Boolean) param);
                } else if (param instanceof java.math.BigDecimal) {
                    statement.setBigDecimal(i + 1, (java.math.BigDecimal) param);
                } else if (param instanceof LocalDateTime) {
                    statement.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) param));
                } else if (param instanceof java.time.LocalDate) {
                    statement.setDate(i + 1, java.sql.Date.valueOf((java.time.LocalDate) param));
                } else if (param instanceof java.time.LocalTime) {
                    statement.setTime(i + 1, java.sql.Time.valueOf((java.time.LocalTime) param));
                } else if (param == null) {
                    statement.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    statement.setObject(i + 1, param);
                }
            }
        }
    }

    /**
     * Extract list of values from ResultSet using a mapper
     */
    protected <T> List<T> mapResultSetToList(ResultSet rs, ResultSetMapper<T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            results.add(mapper.map(rs));
        }

        return results;
    }

    /**
     * Functional interface for mapping ResultSet to objects
     */
    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}