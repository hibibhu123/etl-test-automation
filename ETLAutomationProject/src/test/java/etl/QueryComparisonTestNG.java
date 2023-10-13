package etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class QueryComparisonTestNG {

	private String jdbcUrl;
	private String username;
	private String password;
	private String query1;
	private String query2;
	private String queryFilePath1;
	private String queryFilePath2;
	private List<List<String>> result1;
	private List<List<String>> result2;

	@DataProvider
	public Object[] getFolderPath() {

		File file = new File("./testcases");
		String[] subfolders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		Object[] obj = new Object[subfolders.length];
		List<String> folderList = new ArrayList<String>();

		for (String s : subfolders) {
			folderList.add(s);
		}
		obj = folderList.toArray(obj);

		//System.out.println(Arrays.toString(obj));

		return obj;

	}

	@BeforeTest
	public void setup() {
		jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		username = "scott";
		password = "tiger";

	}

	@Test(dataProvider = "getFolderPath")
	public void testListsEquality(String testCasePath) {
		
		queryFilePath1= "./testcases/"+testCasePath+"/"+"source.sql";
		queryFilePath2= "./testcases/"+testCasePath+"/"+"target.sql";
		
		//queryFilePath1 = "./testcases/Validate_Count/source.sql";
		//queryFilePath2 = "./testcases/Validate_Count/target.sql";

		// Read queries from files
		query1 = readQueryFromFile(queryFilePath1);
		query2 = readQueryFromFile(queryFilePath2);
		result1 = QueryComparisonTestNG.executeQuery(jdbcUrl, username, password, query1);
		result2 = QueryComparisonTestNG.executeQuery(jdbcUrl, username, password, query2);
		// List<Integer> differingRows = findDifferingRows(result1, result2);
		// Find differing rows
		List<Integer> differingRows = QueryComparisonTestNG.findDifferingRows(result1, result2);

		// Assert that the lists are equal
		Assert.assertTrue(differingRows.isEmpty(),
				QueryComparisonTestNG.getDifferencesAsString(result1, result2, differingRows));
		// Assert.assertTrue(differingRows.isEmpty(),"There are mismatchinging records
		// ");

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

	private static String getDifferencesAsString(List<List<String>> list1, List<List<String>> list2,
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

				// if (!value1.equals(value2)) {
				if (!value1.equals(value2)) {
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

	private static String readQueryFromFile(String filePath) {
		StringBuilder query = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;

			while ((line = reader.readLine()) != null) {
				query.append(line).append("\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return query.toString();
	}

}
