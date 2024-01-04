// MySQLDatabase.java
package databaseFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger; // Import the Logger class

public class MySQLDatabase implements Database {

    private static final Logger l = Logger.getLogger(MySQLDatabase.class); // Create a Logger instance

    private String metaDataFilePath;
    private String metaDataQuery;

    @Override
    public void setupConstants(Properties prop, String metaDataFilePath, String metaDataQuery) {
        this.metaDataFilePath = metaDataFilePath;
        this.metaDataQuery = metaDataQuery;
        l.info("MySQLDatabase setup constants completed.");
        // Any other specific setup for MySQLDatabase
    }

    @Override
    public Connection createConnection(String jdbcUrl, String username, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        l.info("MySQLDatabase connection created successfully.");
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            l.info("MySQLDatabase connection closed.");
        }
    }
}
