package testCases;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.TestBase;
import queryFunction.CSVFileReader;
import queryFunction.sqlFunction;
import util.Constants;

public class DataCompletenessValidation extends TestBase {

	private String sourceQuery;
	private String targetQuery;
	private String sourceQueryFilePath;
	private String targetQueryFilePath;
	private List<List<String>> sourceQueryResult;
	private List<List<String>> targetQueryResult;

	private <T> Future<T> submitTask(Callable<T> task) {
		return TestBase.threadPool.submit(task);
	}

	@Test(dataProvider = "getFolderPath", testName = "testFolderBasedTest")
	public void dataCompleteness(String testCasePath) {
		try {

			// Create a task (implementing Callable) for parallel execution
			Callable<Void> dataCompletenessTask = () -> {
				// Your existing test case logic goes here
				try {
					File subfolder = new File(Constants.sqlFilePath + "/" + testCasePath);

					// Check if there's a CSV file and no source.sql
					File[] csvFiles = subfolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
					File sourceSqlFile = new File(subfolder, "source.sql");

					if (csvFiles != null && csvFiles.length > 0 && !sourceSqlFile.exists()) {
						// Read data from the CSV file
						List<List<String>> fileData = CSVFileReader.readCSVFile(csvFiles[0].getPath());
						sourceQueryResult = fileData;
					} else {
						// Read queries from files
						sourceQueryFilePath = Constants.sqlFilePath + "/" + testCasePath + "/" + "source" + ".sql";
						sourceQuery = sqlFunction.readQueryFromFile(sourceQueryFilePath);
						sourceQueryResult = sqlFunction.executeQuery(jdbcUrl, username, password, sourceQuery,
								sourceConnection);
					}

					targetQueryFilePath = Constants.sqlFilePath + "/" + testCasePath + "/" + "target" + ".sql";
					targetQuery = sqlFunction.readQueryFromFile(targetQueryFilePath);
					// Execute SQL queries
					targetQueryResult = sqlFunction.executeQuery(jdbcUrl, username, password, targetQuery,
							targetConnection);

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
				return null;
			};

			// Submit the task to the thread pool
			Future<Void> futureResult = submitTask(dataCompletenessTask);

			// Wait for the task to complete
			futureResult.get();
		} catch (Exception e) {
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
