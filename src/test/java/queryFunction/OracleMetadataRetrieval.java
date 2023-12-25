package queryFunction;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger; // Import the Logger class
import util.Constants;

public class OracleMetadataRetrieval implements DatabaseMetadataRetrieval {

    private static final Logger l = Logger.getLogger(OracleMetadataRetrieval.class); // Create a Logger instance

    @Override
    public List<Map<String, Object>> getTableMetadata(String tableName, Connection connection) throws SQLException {
        List<Map<String, Object>> metadataList = new ArrayList<>();

        try {
            // Build the custom query using the Constants class
            String query = Constants.metaDataQuery_oracle + "'" + tableName + "'";
            l.info("Executing Oracle metadata retrieval query: " + query);

            // Create a statement and execute the query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Map<String, Object> columnMetadata = new HashMap<>();
                columnMetadata.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
                columnMetadata.put("DATA_TYPE", getOracleColumnType(resultSet));

                metadataList.add(columnMetadata);
            }

            // Sort the metadataList based on COLUMN_NAME (case-insensitive)
            metadataList.sort(Comparator.comparing(m -> m.get("COLUMN_NAME").toString(), String.CASE_INSENSITIVE_ORDER));
            l.info("Oracle metadata retrieval completed successfully. Sorted metadataList: " + metadataList);

        } catch (SQLException e) {
            l.error("Error during Oracle metadata retrieval: " + e.getMessage());
            e.printStackTrace();
        }

        return metadataList;
    }

    @Override
    public boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn) {
        // Use Oracle-specific logic for metadata comparison
        // You can add more checks or modify the comparison as needed
        return sourceColumn.get("COLUMN_NAME").equals(targetColumn.get("COLUMN_NAME"))
                && sourceColumn.get("DATA_TYPE").equals(targetColumn.get("DATA_TYPE"));
    }

    private String getOracleColumnType(ResultSet resultSet) throws SQLException {
        String dataType = resultSet.getString("DATA_TYPE");
        String dataLength = resultSet.getString("DATA_LENGTH");

        if ("NUMBER".equals(dataType)) {
            int precision = resultSet.getInt("DATA_PRECISION");
            int scale = resultSet.getInt("DATA_SCALE");

            if (scale > 0) {
                return "NUMBER(" + precision + "," + scale + ")";
            } else if (precision > 0) {
                return "NUMBER(" + precision + ")";
            } else {
                return "NUMBER("+dataLength+")"; // Default case if precision and scale are not available
            }
        } else if ("DATE".equals(dataType)) {
            return "DATE";
        } else if ("VARCHAR2".equals(dataType) || "NVARCHAR2".equals(dataType) || "CHAR".equals(dataType)) {
            int byteLength = resultSet.getInt("DATA_LENGTH");
            return "VARCHAR2(" + byteLength + ")";
        }

        // Add more cases for other data types as needed

        // Default case (if no specific handling is implemented)
        return resultSet.getString("DATA_LENGTH");
    }
}
