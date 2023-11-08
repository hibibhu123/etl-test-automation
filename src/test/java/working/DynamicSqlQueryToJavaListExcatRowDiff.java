package working;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DynamicSqlQueryToJavaListExcatRowDiff {
	public static void main(String[] args) {
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

		// Example queries
		String query1 = "SELECT * FROM emp";
		String query2 = "SELECT * FROM emp";

		// Execute and print results for Query 1
		List<List<String>> result1 = executeQuery(jdbcUrl, username, password, query1);
		//System.out.println("Result for Query 1:");
		//printResult(result1);

		// Execute and print results for Query 2
		List<List<String>> result2 = executeQuery(jdbcUrl, username, password, query2);
		//System.out.println("Result for Query 2:");
		//printResult(result2);

		List<Integer> differingRows = findDifferingRows(result1, result2);

		// Print the result of the comparison
		if (differingRows.isEmpty()) {
			System.out.println("The lists are equal.");
		} else {
			System.out.println("The lists are not equal. Differences in rows: " + differingRows);
			printDifferences(result1, result2, differingRows);
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

	private static void printDifferences(List<List<String>> list1, List<List<String>> list2,
			List<Integer> differingRows) {
		System.out.println("Details of Differences:");

		for (int rowNum : differingRows) {
			List<String> row1 = list1.get(rowNum - 1);
			List<String> row2 = list2.get(rowNum - 1);

			System.out.println("Row " + rowNum + ":");

			// Compare each value in the rows and print only differences
			boolean rowDiffers = false;
			for (int j = 0; j < row1.size(); j++) {
				String value1 = row1.get(j);
				String value2 = row2.get(j);

				if (!value1.equals(value2)) {
					rowDiffers = true;
					System.out.println("   Column " + (j + 1) + ": List1 = " + value1 + ", List2 = " + value2);
				}
			}

			if (!rowDiffers) {
				System.out.println("   The rows are identical.");
			}
		}
	}

	private static List<Integer> findDifferingRows(List<List<String>> list1, List<List<String>> list2) {
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
