package com.sunrisedental.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Database configuration and connection management.
 * Implements Singleton pattern for efficient connection handling.
 */
public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private static Properties properties;

    private DatabaseConfig() {
        loadProperties();
        loadDriver();
    }

    /**
     * Get singleton instance of DatabaseConfig
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Load database properties from configuration file
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                logger.error("Unable to find db.properties");
                throw new RuntimeException("Database configuration file not found");
            }
            properties.load(input);
            logger.info("Database properties loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading database properties", e);
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Load JDBC driver
     */
    private void loadDriver() {
        try {
            Class.forName(properties.getProperty("db.driver"));
            logger.info("JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("JDBC Driver not found", e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        try {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");

            Connection connection = DriverManager.getConnection(url, user, password);
            logger.debug("Database connection established");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw e;
        }
    }

    /**
     * Close database connection safely
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    /**
     * Get database properties
     */
    public Properties getProperties() {
        return properties;
    }
}