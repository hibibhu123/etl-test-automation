package testCases;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import base.TestBase;
import queryFunction.sqlFunction;
import util.Constants;

public class DataCompletenessValidation extends TestBase{

	private String query1;
	private String query2;
	private String queryFilePath1;
	private String queryFilePath2;
	private List<List<String>> result1;
	private List<List<String>> result2;


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
