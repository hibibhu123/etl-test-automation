package working;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PrintSQLQueryResult {

    public static void main(String[] args) {
        // Replace these with your database connection details
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

        // Replace this with your SQL query
        String query = "SELECT EMPNO, ENAME FROM emp";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Execute the query
            ResultSet resultSet = executeQuery(connection, query);

            // Print the column headers
            printColumnHeaders(resultSet);

            // Print the result set
            printResultSet(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ResultSet executeQuery(Connection connection, String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    private static void printColumnHeaders(ResultSet resultSet) throws SQLException {
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            System.out.print(resultSet.getMetaData().getColumnName(i) + "\t");
        }
        System.out.println(); // Move to the next line after printing column headers
    }

    private static void printResultSet(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            // Print each column value for the current row
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                System.out.print(resultSet.getObject(i) + "\t");
            }
            System.out.println(); // Move to the next line after printing a row
        }
    }
}
