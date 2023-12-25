package queryFunction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger; // Import the Logger class

public class sqlFunction {

    private static final Logger l = Logger.getLogger(sqlFunction.class); // Create a Logger instance

    public static String readQueryFromFile(String filePath) {
        StringBuilder query = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                query.append(line).append("\n");
            }

            l.info("Query read from file: " + filePath);

        } catch (IOException e) {
            l.error("Error reading query from file: " + e.getMessage());
            e.printStackTrace();
        }

        return query.toString();
    }

    public static List<List<String>> executeQuery(String jdbcUrl, String username, String password, String query,
            Connection connection) throws SQLException {
        List<List<String>> resultList = new ArrayList<>();

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                List<String> rowValues = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i).toUpperCase();
                    String columnValue = resultSet.getString(i);

                    // System.out.println(columnName+ " "+columnValue);

                    // Handle null values if needed
                    if (resultSet.wasNull()) {
                        columnValue = "NULL"; // or any other placeholder
                    }

                    rowValues.add(columnName + ": " + columnValue);
                }

                resultList.add(rowValues);
            }

            l.info("Query executed successfully: " + query);

        } catch (SQLException e) {
            l.error("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }

        return resultList;
    }

    public static String getDifferencesAsString(List<List<String>> list1, List<List<String>> list2,
            List<Integer> differingRows) {
        StringBuilder result = new StringBuilder("Details of Differences:\n");

        for (int rowNum : differingRows) {
            List<String> row1 = list1.get(rowNum - 1);
            List<String> row2 = list2.get(rowNum - 1);

            result.append("Row ").append(rowNum).append(":\n");

            // Compare each value in the rows and append only differences
            boolean rowDiffers = false;
            for (int j = 0; j < row1.size(); j++) {
                String value1 = row1.get(j);
                String value2 = row2.get(j);

                // System.out.println(value1);
                // System.out.println(value2);

                // if (!value1.equals(value2)) {
                if (!value1.trim().equals(value2.trim())) {
                    rowDiffers = true;
                    result.append("   Column ").append(j + 1).append(": List1 = ").append(value1).append(", List2 = ")
                            .append(value2).append("\n");
                }
            }

            /*
             * if (!rowDiffers) { result.append("   The rows are identical.\n"); }
             */
        }

        return result.toString();
    }

    public static List<Integer> findDifferingRows(List<List<String>> list1, List<List<String>> list2) {
        List<Integer> differingRows = new ArrayList<>();

        // Compare sizes first
        if (list1.size() != list2.size()) {
            // If sizes are different, mark all rows as differing
            for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
                differingRows.add(i + 1);
            }
            return differingRows;
        }

        // Compare each element in the lists
        for (int i = 0; i < list1.size(); i++) {
            List<String> row1 = list1.get(i);
            List<String> row2 = list2.get(i);

            // Compare sizes of rows
            if (row1.size() != row2.size()) {
                differingRows.add(i + 1);
                continue;
            }

            // Compare each value in the rows
            for (int j = 0; j < row1.size(); j++) {
                if (!row1.get(j).equals(row2.get(j))) {
                    differingRows.add(i + 1);
                    break; // Move to the next row
                }
            }
        }

        return differingRows;
    }
}
