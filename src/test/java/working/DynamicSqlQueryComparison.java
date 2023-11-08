package working;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DynamicSqlQueryComparison {

    public static void main(String[] args) {
        // Replace these with your database connection details
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

        // Replace these with the file paths of your SQL queries
        String sourceQueryPath = "./testcases/source/source.sql";
        String targetQueryPath = "./testcases/target/target.sql";

        // Execute and compare queries
        executeAndCompareQueries(jdbcUrl, username, password, sourceQueryPath, targetQueryPath);
    }

    private static void executeAndCompareQueries(String jdbcUrl, String username, String password,
                                                 String sourceQueryPath, String targetQueryPath) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Execute source query
            List<List<String>> sourceResults = executeQueryAndGetResults(connection, readQueryFromFile(sourceQueryPath));

            // Execute target query
            List<List<String>> targetResults = executeQueryAndGetResults(connection, readQueryFromFile(targetQueryPath));

            // Compare results
            boolean areResultsEqual = compareResults(sourceResults, targetResults);

            // Print the result of the comparison
            if (areResultsEqual) {
                System.out.println("The SQL query results are equal.");
            } else {
                System.out.println("The SQL query results are not equal. Differences:");
                printDifferences(sourceResults, targetResults);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static List<List<String>> executeQueryAndGetResults(Connection connection, String query) throws SQLException {
        List<List<String>> results = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                List<String> rowValues = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    rowValues.add(value);
                }

                results.add(rowValues);
            }
        }

        return results;
    }

    private static boolean compareResults(List<List<String>> sourceResults, List<List<String>> targetResults) {
        // Compare sizes first
        if (sourceResults.size() != targetResults.size()) {
            return false;
        }

        // Compare each element in the lists in both directions
        for (int i = 0; i < sourceResults.size(); i++) {
            List<String> sourceRow = sourceResults.get(i);
            List<String> targetRow = targetResults.get(i);

            // Compare sizes of rows
            if (sourceRow.size() != targetRow.size()) {
                return false;
            }

            // Compare each value in the rows in both directions
            boolean rowDifferentSourceToTarget = false;
            boolean rowDifferentTargetToSource = false;
            for (int j = 0; j < sourceRow.size(); j++) {
                if (!sourceRow.get(j).equals(targetRow.get(j))) {
                    rowDifferentSourceToTarget = true;
                }
                if (!targetRow.get(j).equals(sourceRow.get(j))) {
                    rowDifferentTargetToSource = true;
                }
            }

            // Check if there's a difference in either direction
            if (!rowDifferentSourceToTarget && !rowDifferentTargetToSource) {
                return false;  // Rows are identical, move to the next row
            }
        }

        return true;  // All rows are different in at least one direction
    }

    private static void printDifferences(List<List<String>> sourceResults, List<List<String>> targetResults) {
        for (int i = 0; i < sourceResults.size(); i++) {
            List<String> sourceRow = sourceResults.get(i);
            List<String> targetRow = targetResults.get(i);

            System.out.println("Row " + (i + 1) + ":");

            // Compare each value in the rows
            for (int j = 0; j < sourceRow.size(); j++) {
                String sourceValue = sourceRow.get(j);
                String targetValue = targetRow.get(j);

                System.out.println("   Column " + (j + 1) + ": Source = " + sourceValue + ", Target = " + targetValue);
            }
        }
    }
    private static String readQueryFromFile(String filePath) throws IOException {
        StringBuilder query = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                query.append(line).append("\n");
            }

        }

        return query.toString();
    }
}
