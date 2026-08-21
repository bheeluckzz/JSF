package com.rsifatimah.keperawatan.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtil {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());

    private static String url = "jdbc:postgresql://127.0.0.1:5432/db_nursing";
    private static String username = "postgres";
    private static String password = "";

    static {
        try {
            Class.forName("org.postgresql.Driver");
            loadConfig();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "PostgreSQL JDBC Driver not found!", e);
        }
    }

    private static void loadConfig() {
        try (InputStream is = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties prop = new Properties();
                prop.load(is);
                url = prop.getProperty("db.url", url);
                username = prop.getProperty("db.username", username);
                password = prop.getProperty("db.password", password);
                LOGGER.info("db.properties loaded successfully: URL=" + url + ", USER=" + username);
            } else {
                LOGGER.warning("db.properties not found in classpath. Using default settings.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load db.properties, using fallback settings.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed connecting with configured password, trying alternative passwords...", e);
            // Fallback trial if default password was used and rejected (e.g. empty string or root)
            String[] fallbacks = new String[]{"", "root", "admin", "postgres"};
            for (String fallbackPass : fallbacks) {
                if (!fallbackPass.equals(password)) {
                    try {
                        Connection conn = DriverManager.getConnection(url, username, fallbackPass);
                        LOGGER.info("Connected to PostgreSQL using fallback password: '" + fallbackPass + "'");
                        password = fallbackPass; // update to working password
                        return conn;
                    } catch (SQLException ignored) {
                    }
                }
            }
            throw e;
        }
    }
}
