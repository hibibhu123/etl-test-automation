package queryFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConn {

	private static Connection connection;

	public static void closeConnection(Connection conn) {
		try {
			conn.close();
			System.out.println("Database Connection Closed");
		} catch (SQLException e) {
			System.out.println(e.toString());
		}
	}

	public static Connection createConnection(String jdbcUrl, String username, String password) {

		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return connection;

	}

}
