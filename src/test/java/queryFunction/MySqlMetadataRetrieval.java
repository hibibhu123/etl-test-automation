package queryFunction;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.Constants; // Import the Constants class

public class MySqlMetadataRetrieval implements DatabaseMetadataRetrieval {
    @Override
    public List<Map<String, Object>> getTableMetadata(String tableName, Connection connection) throws SQLException {
        List<Map<String, Object>> metadataList = new ArrayList<>();

        try {
            // Build the custom query using the Constants class
            String query = Constants.metaDataQuery_mysql + "'" + tableName + "'";

            // Create a statement and execute the query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> columnMetadata = new HashMap<>();
                columnMetadata.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
                columnMetadata.put("DATA_TYPE", resultSet.getString("DATA_TYPE"));
                columnMetadata.put("DATA_LENGTH", resultSet.getString("COLUMN_TYPE"));

                metadataList.add(columnMetadata);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Metadata from MySQL Table:" + metadataList);
        return metadataList;
    }

    @Override
    public boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn) {
        // Compare ignoring leading/trailing whitespaces
        boolean columnNameMatch = sourceColumn.get("COLUMN_NAME").toString().trim()
                .equals(targetColumn.get("COLUMN_NAME").toString().trim());
        boolean dataTypeMatch = sourceColumn.get("DATA_TYPE").toString().trim()
                .equals(targetColumn.get("DATA_TYPE").toString().trim());
        boolean dataLengthMatch = sourceColumn.get("DATA_LENGTH").toString().trim()
                .equals(targetColumn.get("DATA_LENGTH").toString().trim());

        return columnNameMatch && dataTypeMatch && dataLengthMatch;
    }
}
