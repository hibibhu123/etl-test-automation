package working;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DynamicSqlQueryToJavaList_SingleList {
    public static void main(String[] args) {
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM emp")) {

            List<List<String>> resultList = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                List<String> rowValues = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(i);

                    // Handle null values if needed
                    if (resultSet.wasNull()) {
                        columnValue = "NULL"; // or any other placeholder
                    }

                    rowValues.add(columnName + ": " + columnValue);
                }

                resultList.add(rowValues);
            }

            // Do something with the resultList
            for (List<String> row : resultList) {
                System.out.println(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
