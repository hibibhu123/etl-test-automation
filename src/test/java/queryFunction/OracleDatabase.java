package queryFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger; // Import the Logger class

public class OracleDatabase implements Database {

    private static final Logger l = Logger.getLogger(OracleDatabase.class); // Create a Logger instance

    private String metaDataFilePath;
    private String metaDataQuery;

    @Override
    public void setupConstants(Properties prop, String metaDataFilePath, String metaDataQuery) {
        this.metaDataFilePath = metaDataFilePath;
        this.metaDataQuery = metaDataQuery;
        // Any other specific setup for OracleDatabase
        l.info("OracleDatabase constants set up completed.");
    }

    @Override
    public Connection createConnection(String jdbcUrl, String username, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        l.info("OracleDatabase connection created successfully.");
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            l.info("OracleDatabase connection closed.");
        }
    }
}
