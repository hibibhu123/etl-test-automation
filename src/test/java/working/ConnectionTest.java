package working;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionTest {

	public static void main(String[] args) {
		// JDBC URL, username, and password of Oracle database
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

		// Establish a connection
		try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
			if (connection != null) {
				System.out.println("Connected to the database!");
				// Your database operations go here
			} else {
				System.out.println("Failed to connect to the database!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}