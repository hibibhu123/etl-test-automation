package testCases;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
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

            l.info("Executing dataCompleteness for testCasePath: " + testCasePath);

            // Create a task (implementing Callable) for parallel execution
            Callable<Void> dataCompletenessTask = () -> {
                try {
                    File source_subfolder = new File(Constants.sqlFilePath + "/" + testCasePath + "/source");
                    File target_subfolder = new File(Constants.sqlFilePath + "/" + testCasePath + "/target");

                    File[] source_csvFiles = source_subfolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
                    File sourceSqlFile = new File(source_subfolder, "source.sql");

                    File[] target_csvFiles = target_subfolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
                    File targetSqlFile = new File(target_subfolder, "target.sql");

                    if (source_csvFiles != null && source_csvFiles.length > 0 && !sourceSqlFile.exists()) {
                        l.info("Reading data from source CSV file");
                        List<List<String>> fileData = CSVFileReader.readCSVFile(source_csvFiles[0].getPath());
                        sourceQueryResult = fileData;

                    } else {
                        l.info("Reading source SQL query from file");
                        sourceQueryFilePath = Constants.sqlFilePath + "/" + testCasePath + "/source/" + "source" + ".sql";
                        sourceQuery = sqlFunction.readQueryFromFile(sourceQueryFilePath);
                        sourceQueryResult = sqlFunction.executeQuery(jdbcUrl, username, password, sourceQuery,
                                sourceConnection);
                    }

                    if (target_csvFiles != null && target_csvFiles.length > 0 && !targetSqlFile.exists()) {
                        l.info("Reading data from target CSV file");
                        List<List<String>> fileData = CSVFileReader.readCSVFile(target_csvFiles[0].getPath());
                        targetQueryResult = fileData;

                    } else {
                        l.info("Reading target SQL query from file");
                        targetQueryFilePath = Constants.sqlFilePath + "/" + testCasePath + "/target/" + "target" + ".sql";
                        targetQuery = sqlFunction.readQueryFromFile(targetQueryFilePath);
                        targetQueryResult = sqlFunction.executeQuery(jdbcUrl, username, password, targetQuery,
                                targetConnection);
                    }

                    l.info("Finding differing rows");
                    List<Integer> differingRows = sqlFunction.findDifferingRows(sourceQueryResult, targetQueryResult);

                    l.info("Asserting that the lists are equal");
                    Assert.assertTrue(differingRows.isEmpty(),
                            sqlFunction.getDifferencesAsString(sourceQueryResult, targetQueryResult, differingRows));
                } catch (SQLException sqlException) {
                    // SQL syntax error detected
                    String errorMessage = "SQL Syntax Error: " + sqlException.getMessage();
                    l.error(errorMessage, sqlException);
                    throw new AssertionError(errorMessage, sqlException);
                } catch (Exception e) {
                    l.error("Test failed: " + e.getMessage(), e);
                    throw new AssertionError("Test failed: " + e.getMessage(), e);
                }
                return null;
            };

            // Submit the task to the thread pool
            Future<Void> futureResult = submitTask(dataCompletenessTask);

            // Wait for the task to complete
            futureResult.get();
        } catch (Exception e) {
            l.error("Test failed: " + e.getMessage(), e);
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
            l.error("Skipping the test due to an exception in the data provider: " + e.getMessage(), e);
            System.out.println("Skipping the test due to an exception in the data provider: " + e.getMessage());
            return new Object[0]; // Return an empty array to indicate test failure
        }
    }
}
