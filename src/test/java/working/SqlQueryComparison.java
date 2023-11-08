package working;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlQueryComparison {

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
            List<String> sourceResults = executeQueryAndGetResults(connection, readQueryFromFile(sourceQueryPath));

            // Execute target query
            List<String> targetResults = executeQueryAndGetResults(connection, readQueryFromFile(targetQueryPath));

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

    private static List<String> executeQueryAndGetResults(Connection connection, String query) throws SQLException {
        List<String> results = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                // Assuming a single column result; adjust as needed
                String value = resultSet.getString(1);
                results.add(value);
            }
        }

        return results;
    }

    private static boolean compareResults(List<String> sourceResults, List<String> targetResults) {
        // Compare sizes first
        if (sourceResults.size() != targetResults.size()) {
            return false;
        }

        // Compare each element in the lists
        for (int i = 0; i < sourceResults.size(); i++) {
            if (!sourceResults.get(i).equals(targetResults.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static void printDifferences(List<String> sourceResults, List<String> targetResults) {
        for (int i = 0; i < sourceResults.size(); i++) {
            String sourceValue = sourceResults.get(i);
            String targetValue = targetResults.get(i);

            if (!sourceValue.equals(targetValue)) {
                System.out.println("Row " + (i + 1) + ": Source = " + sourceValue + ", Target = " + targetValue);
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
