// OracleDatabase.java
package queryFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class OracleDatabase implements Database {

    private String metaDataFilePath;
    private String metaDataQuery;

    @Override
    public void setupConstants(Properties prop, String metaDataFilePath, String metaDataQuery) {
        this.metaDataFilePath = metaDataFilePath;
        this.metaDataQuery = metaDataQuery;
        // Any other specific setup for OracleDatabase
    }

    @Override
    public Connection createConnection(String jdbcUrl, String username, String password) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
