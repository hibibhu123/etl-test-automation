package working;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DynamicSqlQueryToJavaList2Lists {
    public static void main(String[] args) {
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

        // Example queries
        String query1 = "SELECT * FROM emp";
        String query2 = "SELECT * FROM t_emp";

        // Execute and print results for Query 1
        List<List<String>> result1 = executeQuery(jdbcUrl, username, password, query1);
        System.out.println("Result for Query 1:");
        printResult(result1);

        // Execute and print results for Query 2
        List<List<String>> result2 = executeQuery(jdbcUrl, username, password, query2);
        System.out.println("Result for Query 2:");
        printResult(result2);
        
        boolean areEqual = compareLists(result1, result2);

        // Print the result of the comparison
        if (areEqual) {
            System.out.println("The lists are equal.");
        } else {
            System.out.println("The lists are not equal. Differences:");
            printDifferences(result1, result2);
        }
        
    }

    private static List<List<String>> executeQuery(String jdbcUrl, String username, String password, String query) {
        List<List<String>> resultList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    private static void printResult(List<List<String>> resultList) {
        for (List<String> row : resultList) {
            System.out.println(row);
        }
    }
    private static boolean compareLists(List<List<String>> list1, List<List<String>> list2) {
        // Compare sizes first
        if (list1.size() != list2.size()) {
            return false;
        }

        // Compare each element in the lists
        for (int i = 0; i < list1.size(); i++) {
            List<String> row1 = list1.get(i);
            List<String> row2 = list2.get(i);

            // Compare sizes of rows
            if (row1.size() != row2.size()) {
                return false;
            }

            // Compare each value in the rows
            for (int j = 0; j < row1.size(); j++) {
                if (!row1.get(j).equals(row2.get(j))) {
                    return false;
                }
            }
        }

        return true;  // Lists are identical
    }

    private static void printDifferences(List<List<String>> list1, List<List<String>> list2) {
        for (int i = 0; i < list1.size(); i++) {
            List<String> row1 = list1.get(i);
            List<String> row2 = list2.get(i);

            System.out.println("Difference in Row " + (i + 1) + ":");

            // Compare each value in the rows
            for (int j = 0; j < row1.size(); j++) {
                String value1 = row1.get(j);
                String value2 = row2.get(j);

                System.out.println("   Column " + (j + 1) + ": List1 = " + value1 + ", List2 = " + value2);
            }
        }
    }
}
