package campusfind.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory for JDBC connections. Every call to getConnection() returns a NEW connection,
 * which the caller closes with try-with-resources (connection-per-operation).
 * Settings come from db.properties (or from the file named by -Dcampusfind.config=...).
 */
public final class DBConnection {
    private static Properties props;

    private DBConnection() { }

    private static synchronized Properties props() {
        if (props == null) {
            Properties p = new Properties();
            String external = System.getProperty("campusfind.config");
            try (InputStream in = external != null
                    ? new FileInputStream(external)
                    : DBConnection.class.getResourceAsStream("/db.properties")) {
                if (in == null) {
                    throw new IllegalStateException("db.properties was not found");
                }
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read db.properties: " + e.getMessage(), e);
            }
            props = p;
        }
        return props;
    }

    public static String get(String key, String defaultValue) {
        return props().getProperty(key, defaultValue).trim();
    }

    private static String url(String databaseName) {
        String params = get("db.params", "");
        return "jdbc:" + get("db.scheme", "mysql") + "://" + get("db.host", "localhost") + ":"
                + get("db.port", "3306") + "/" + databaseName
                + (params.isEmpty() ? "" : "?" + params);
    }

    /** A new connection to the CampusFind database. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url(get("db.name", "campusfind")),
                get("db.user", "root"), get("db.password", ""));
    }

    /** A new connection to the MySQL server itself (no database selected), used to create the database. */
    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(url(""), get("db.user", "root"), get("db.password", ""));
    }
}
