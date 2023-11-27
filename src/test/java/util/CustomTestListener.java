package util;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class CustomTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        // Handle test start if needed
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testCaseName = result.getMethod().getMethodName();
        
        if (testCaseName.startsWith("testFolderBased")) {
            handleFolderBasedTestSuccess(testCaseName);
        } else if (testCaseName.startsWith("testMetadataComparison")) {
            handleMetadataBasedTestSuccess(testCaseName);
        }
        // Add additional conditions if needed for other types of tests
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testCaseName = result.getMethod().getMethodName();
        
        if (testCaseName.startsWith("testFolderBased")) {
            handleFolderBasedTestFailure(testCaseName);
        } else if (testCaseName.startsWith("testMetadataComparison")) {
            handleMetadataBasedTestFailure(testCaseName);
        }
        // Add additional conditions if needed for other types of tests
    }

    // Implement other methods as needed

    private void handleFolderBasedTestSuccess(String testCaseName) {
        String folderName = getFolderNameFromSQL(testCaseName);
        String resultMessage = "Test Case: " + folderName + " - PASSED";
        System.out.println(resultMessage);
        // Log the result or generate a report as needed
    }

    private void handleFolderBasedTestFailure(String testCaseName) {
        String folderName = getFolderNameFromSQL(testCaseName);
        String resultMessage = "Test Case: " + folderName + " - FAILED";
        System.out.println(resultMessage);
        // Log the result or generate a report as needed
    }

    private void handleMetadataBasedTestSuccess(String testCaseName) {
        // Implement logic for metadata-based test success
        // Replace this with your actual logic
        String resultMessage = "Metadata Test Case: " + testCaseName + " - PASSED";
        System.out.println(resultMessage);
        // Log the result or generate a report as needed
    }

    private void handleMetadataBasedTestFailure(String testCaseName) {
        // Implement logic for metadata-based test failure
        // Replace this with your actual logic
        String resultMessage = "Metadata Test Case: " + testCaseName + " - FAILED";
        System.out.println(resultMessage);
        // Log the result or generate a report as needed
    }

    private String getFolderNameFromSQL(String testCaseName) {
        // Implement logic to fetch folder name from SQL based on the test case name
        // Replace this with your actual logic
        return "FolderName";
    }
}
