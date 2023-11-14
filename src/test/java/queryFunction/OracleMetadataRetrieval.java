package queryFunction;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Constants;

public class OracleMetadataRetrieval implements DatabaseMetadataRetrieval {
	@Override
	public List<Map<String, Object>> getTableMetadata(String tableName, Connection connection) throws SQLException {
		List<Map<String, Object>> metadataList = new ArrayList<>();

		try {
			// Build the custom query using the Constants class
			String query = Constants.metaDataQuery_oracle + "'" + tableName + "'";

			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				Map<String, Object> columnMetadata = new HashMap<>();
				columnMetadata.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
				columnMetadata.put("DATA_TYPE", resultSet.getString("DATA_TYPE"));

				// Customize the data length based on DATA_TYPE, DATA_PRECISION, and DATA_SCALE
				String dataLength = getOracleDataLength(resultSet);
				columnMetadata.put("DATA_LENGTH", dataLength);

				metadataList.add(columnMetadata);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Metadata from Oracle Table:" + metadataList);
		return metadataList;
	}

	@Override
	public boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn) {
		// Use Oracle-specific logic for metadata comparison
		// You can add more checks or modify the comparison as needed
		return sourceColumn.get("COLUMN_NAME").equals(targetColumn.get("COLUMN_NAME"))
				&& sourceColumn.get("DATA_TYPE").equals(targetColumn.get("DATA_TYPE"))
				&& oracleDataLengthMatch(sourceColumn, targetColumn);
	}

	private boolean oracleDataLengthMatch(Map<String, Object> sourceColumn, Map<String, Object> targetColumn) {
		// Implement Oracle-specific logic for data length comparison
		// For example, you can compare the DATA_LENGTH values
		String sourceDataLength = sourceColumn.get("DATA_LENGTH").toString();
		String targetDataLength = targetColumn.get("DATA_LENGTH").toString();

		// Your logic for comparison based on data length
		// For simplicity, I'm comparing them directly here. You may need to adjust
		// this.
		return sourceDataLength.equals(targetDataLength);
	}

	private String getOracleDataLength(ResultSet resultSet) throws SQLException {
		String dataType = resultSet.getString("DATA_TYPE");

		if ("NUMBER".equals(dataType)) {
			int precision = resultSet.getInt("DATA_PRECISION");
			int scale = resultSet.getInt("DATA_SCALE");

			if (scale > 0) {
				return "NUMBER(" + precision + "," + scale + ")";
			} else {
				return "NUMBER(" + precision + ")";
			}
		} else if ("DATE".equals(dataType)) {
			return "DATE";
		} else if ("VARCHAR2".equals(dataType) || "NVARCHAR2".equals(dataType) || "CHAR".equals(dataType)) {
			//int charLength = resultSet.getInt("CHAR_LENGTH");
			int byteLength = resultSet.getInt("DATA_LENGTH");

			// return "VARCHAR2(" + charLength + " CHAR, " + byteLength + " BYTE)";
			return "VARCHAR2(" + byteLength + ")";
		}

		// Add more cases for other data types as needed

		// Default case (if no specific handling is implemented)
		return resultSet.getString("DATA_LENGTH");
	}

}
