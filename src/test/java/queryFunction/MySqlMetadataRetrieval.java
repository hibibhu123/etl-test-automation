package queryFunction;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger; // Import the Logger class
import util.Constants;

public class MySqlMetadataRetrieval implements DatabaseMetadataRetrieval {

    private static final Logger l = Logger.getLogger(MySqlMetadataRetrieval.class); // Create a Logger instance

    @Override
    public List<Map<String, Object>> getTableMetadata(String tableName, Connection connection) throws SQLException {
        List<Map<String, Object>> metadataList = new ArrayList<>();

        try {
            // Updated query with single quotes around the table name
            String query = Constants.metaDataQuery_mysql + "'" + tableName + "'";

            // Create a statement and execute the query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> columnMetadata = new HashMap<>();
                columnMetadata.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
                columnMetadata.put("DATA_TYPE", resultSet.getString("COLUMN_TYPE"));

                metadataList.add(columnMetadata);
            }
            l.info("Metadata retrieval from MySQL table completed for table: " + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
            l.error("Error while retrieving metadata from MySQL table: " + tableName, e);
        }

        // Sort metadataList based on COLUMN_NAME
        Collections.sort(metadataList, (a, b) -> String.valueOf(a.get("COLUMN_NAME"))
                .compareToIgnoreCase(String.valueOf(b.get("COLUMN_NAME"))));

        l.info("Metadata from MySQL Table:" + metadataList);
        return metadataList;
    }

    @Override
    public boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn) {
        // Compare ignoring leading/trailing whitespaces
        boolean columnNameMatch = sourceColumn.get("COLUMN_NAME").toString().trim()
                .equalsIgnoreCase(targetColumn.get("COLUMN_NAME").toString().trim());
        boolean dataTypeMatch = sourceColumn.get("DATA_TYPE").toString().trim()
                .equalsIgnoreCase(targetColumn.get("DATA_TYPE").toString().trim());

        return columnNameMatch && dataTypeMatch;
    }
}
