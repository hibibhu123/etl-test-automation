// Database.java
package queryFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public interface Database {

    void setupConstants(Properties prop, String metaDataFilePath, String metaDataQuery);

    Connection createConnection(String jdbcUrl, String username, String password) throws SQLException;

    void closeConnection(Connection connection) throws SQLException;
}
