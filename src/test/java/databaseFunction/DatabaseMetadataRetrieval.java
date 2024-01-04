package databaseFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public interface DatabaseMetadataRetrieval {
    List<Map<String, Object>> getTableMetadata(String tableName, Connection connection) throws SQLException;

    boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn);
}
