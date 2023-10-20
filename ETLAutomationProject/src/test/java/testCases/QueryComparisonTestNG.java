package testCases;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.scanner.Constant;

import queryFunction.DatabaseConn;
import queryFunction.sqlFunction;
import util.Constants;
import util.PropertyFileReader;

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
	private Properties prop;
	private Connection connection;

	@BeforeTest
	public void setup() {
		try {
			prop = PropertyFileReader.readPropertiesFile(Constants.propertyFilePath);
		} catch (IOException e) {

			e.printStackTrace();
		}
		jdbcUrl = prop.getProperty("jdbcUrl");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		connection = DatabaseConn.createConnection(jdbcUrl, username, password);

	}

	@Test(dataProvider = "getFolderPath")
	public void testListsEquality(String testCasePath) {

		queryFilePath1 = Constants.sqlFilePath + "/" + testCasePath + "/" + "source.sql";
		queryFilePath2 = Constants.sqlFilePath + "/" + testCasePath + "/" + "target.sql";

		// Read queries from files
		query1 = sqlFunction.readQueryFromFile(queryFilePath1);
		query2 = sqlFunction.readQueryFromFile(queryFilePath2);
		result1 = sqlFunction.executeQuery(jdbcUrl, username, password, query1, connection);
		result2 = sqlFunction.executeQuery(jdbcUrl, username, password, query2, connection);
		// Find differing rows
		List<Integer> differingRows = sqlFunction.findDifferingRows(result1, result2);

		// Assert that the lists are equal
		Assert.assertTrue(differingRows.isEmpty(), sqlFunction.getDifferencesAsString(result1, result2, differingRows));

	}

	@AfterTest
	public void close() {
		DatabaseConn.closeConnection(connection);
	}

	@DataProvider
	public Object[] getFolderPath() {

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

	}
}
