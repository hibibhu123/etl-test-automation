package testCases;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.TestBase;
import queryFunction.sqlFunction;
import util.Constants;

public class DataCompletenessValidation extends TestBase {

	private String sourceQuery;
	private String targetQuery;
	private String sourceQueryFilePath;
	private String targetQueryFilePath;
	private List<List<String>> sourceQueryResult;
	private List<List<String>> targetQueryResult;

	@Test(dataProvider = "getFolderPath")
	public void testListsEquality(String testCasePath) {
		try {
			// Determine the source and target database types
			String sourceDBType = prop.getProperty("sourceDB");
			String targetDBType = prop.getProperty("targetDB");

			// Construct the file paths for source and target queries based on the selected
			// database types
			sourceQueryFilePath = Constants.sqlFilePath + "/" + testCasePath + "/" + sourceDBType + "/source.sql";
			targetQueryFilePath = Constants.sqlFilePath + "/" + testCasePath + "/" + targetDBType + "/target.sql";

			// Read queries from files
			sourceQuery = sqlFunction.readQueryFromFile(sourceQueryFilePath);
			targetQuery = sqlFunction.readQueryFromFile(targetQueryFilePath);

			// Execute SQL queries
			sourceQueryResult = sqlFunction.executeQuery(jdbcUrl, username, password, sourceQuery, sourceConnection);
			targetQueryResult = sqlFunction.executeQuery(jdbcUrl, username, password, targetQuery, targetConnection);

			// Find differing rows
			List<Integer> differingRows = sqlFunction.findDifferingRows(sourceQueryResult, targetQueryResult);

			// Assert that the lists are equal
			Assert.assertTrue(differingRows.isEmpty(),
					sqlFunction.getDifferencesAsString(sourceQueryResult, targetQueryResult, differingRows));
		} catch (SQLException sqlException) {
			// SQL syntax error detected
			String errorMessage = "SQL Syntax Error: " + sqlException.getMessage();
			sqlException.printStackTrace();
			throw new AssertionError(errorMessage, sqlException);
		} catch (Exception e) {
			// Handle other exceptions
			e.printStackTrace();
			throw new AssertionError("Test failed: " + e.getMessage(), e);
		}
	}

	@DataProvider
	public Object[] getFolderPath() {
		try {
			File file = new File(Constants.sqlFilePath);
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
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Skipping the test due to an exception in the data provider: " + e.getMessage());
			return new Object[0]; // Return an empty array to indicate test failure
		}
	}

}
