package util;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
//import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;


public class CustomTestListener implements ITestListener {

    private ExtentReports extent;
    private ExtentTest test;

    @Override
    public void onStart(ITestContext context) {
        String reportPath = "./Reports/ExtentReport.html";
        //ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(reportPath);
        ExtentSparkReporter htmlReporter=new ExtentSparkReporter(reportPath) ;
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testCaseName = result.getMethod().getMethodName();
        Object[] parameters = result.getParameters();

        // If there are parameters, append them to the test name
        if (parameters.length > 0) {
            testCaseName += " - " + String.join("_", convertToString(parameters));
        }

        test = extent.createTest(testCaseName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testCaseName = result.getMethod().getMethodName();
        if (testCaseName.startsWith("testFolderBased")) {
            handleFolderBasedTestSuccess(testCaseName);
        } else if (testCaseName.startsWith("tableMetadataComparison")) {
            handleMetadataBasedTestSuccess(testCaseName);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testCaseName = result.getMethod().getMethodName();
        Object[] parameters = result.getParameters();

        // If there are parameters, append them to the test name
        if (parameters.length > 0) {
            testCaseName += " - " + String.join("_", convertToString(parameters));
        }

        if (testCaseName.startsWith("testFolderBased")) {
            handleFolderBasedTestFailure(testCaseName);
        } else if (testCaseName.startsWith("tableMetadataComparison")) {
            handleMetadataBasedTestFailure(testCaseName);
        }

        test.log(Status.FAIL, "Test Case: " + testCaseName + " - FAILED");
        // Add any additional information to the extent report if needed
        test.log(Status.FAIL, "Failure Details: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    private void handleFolderBasedTestSuccess(String testCaseName) {
        String folderName = getFolderNameFromSQL(testCaseName);
        test.log(Status.PASS, "Test Case: " + folderName + " - PASSED");
    }

    private void handleFolderBasedTestFailure(String testCaseName) {
        String folderName = getFolderNameFromSQL(testCaseName);
        test.log(Status.FAIL, "Test Case: " + folderName + " - FAILED");
    }

    private void handleMetadataBasedTestSuccess(String testCaseName) {
        test.log(Status.PASS, "Metadata Test Case: " + testCaseName + " - PASSED");
    }

    private void handleMetadataBasedTestFailure(String testCaseName) {
        test.log(Status.FAIL, "Metadata Test Case: " + testCaseName + " - FAILED");
    }

    private String getFolderNameFromSQL(String testCaseName) {
        return "FolderName";
    }

    private String[] convertToString(Object[] parameters) {
        String[] stringArray = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            stringArray[i] = String.valueOf(parameters[i]);
        }
        return stringArray;
    }
}
