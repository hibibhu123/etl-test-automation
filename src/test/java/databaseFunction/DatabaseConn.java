package databaseFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger; // Import the Logger class

public class DatabaseConn {

    private static final Logger l = Logger.getLogger(DatabaseConn.class); // Create a Logger instance

    private static Connection connection;

    public static void closeConnection(Connection conn) {
        try {
            conn.close();
            l.info("Database Connection Closed");
        } catch (SQLException e) {
            l.error("Error closing database connection: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static Connection createConnection(String jdbcUrl, String username, String password) {

        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            l.info("Database is Connected");
        } catch (SQLException e) {
            l.error("Error creating database connection: " + e.getMessage(), e);
            e.printStackTrace();
        }

        return connection;

    }

}
