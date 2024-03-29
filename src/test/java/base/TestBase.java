package base;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import databaseFunction.Database;
import databaseFunction.DatabaseConn;
import fileFunction.MetadataExcelGenerator;
import util.Banner;
import util.Constants;
import util.CustomTestListener;
import util.Log4jConfigurator;
import util.PropertyFileReader;

@Listeners(CustomTestListener.class)
public class TestBase {

	public Connection targetConnection;
	public Connection sourceConnection;
	public Properties prop;
	public XSSFWorkbook workbook;
	public String jdbcUrl;
	public String username;
	public String password;
	public String metaDataExcelPath;
	public String tableMetaDataQuery;
	public Database database;
	public static ExecutorService threadPool;
	public Logger l = Logger.getLogger(TestBase.class);
	public static int testCaseCounter;
	public static List<String> testResults = new ArrayList<>();

	@BeforeSuite
	public void setUpBeforeSuite() {
		testCaseCounter = 0;
	}

	@AfterSuite
	public void printTestResults() {
		Banner.printLargeResultTextToLog();

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Test Results");
			int rowCount = 0;

			// Create header row with cell styles
			Row headerRow = sheet.createRow(rowCount++);
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			Cell testCaseHeaderCell = headerRow.createCell(0);
			testCaseHeaderCell.setCellValue("Test Case");
			testCaseHeaderCell.setCellStyle(headerStyle);

			Cell statusHeaderCell = headerRow.createCell(1);
			statusHeaderCell.setCellValue("Status");
			statusHeaderCell.setCellStyle(headerStyle);

			// Write test results with color coding
			for (String result : testResults) {
				l.info(result);

				Row row = sheet.createRow(rowCount++);
				String[] resultParts = result.split(": ");
				String testCaseName = resultParts[0];
				String status = resultParts[1];

				Cell testCaseCell = row.createCell(0);
				testCaseCell.setCellValue(testCaseName);

				Cell statusCell = row.createCell(1);
				statusCell.setCellValue(status);

				// Apply color coding
				CellStyle cellStyle = workbook.createCellStyle();
				if ("PASSED".equalsIgnoreCase(status)) {
					cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
				} else if ("FAILED".equalsIgnoreCase(status)) {
					cellStyle.setFillForegroundColor(IndexedColors.RED1.getIndex());
				}
				cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				statusCell.setCellStyle(cellStyle);
			}

			// Auto-size columns to fit content
			for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
				sheet.autoSizeColumn(i);
			}

			try (FileOutputStream outputStream = new FileOutputStream(Constants.testResultExcelFilePath)) {
				workbook.write(outputStream);
			}
			l.info("Test results written to Excel file: " + Constants.testResultExcelFilePath);
		} catch (IOException e) {
			l.error("Error writing test results to Excel", e);
		}
	}

	@BeforeClass
	public void setUp() {

		l.info("BeforeClass setUp() execution started............");
		try {
			Log4jConfigurator.configure();
			l.info("Log4j configured successfully.");
		} catch (Exception e) {
			l.error("Error configuring Log4j", e);
		}
		try {
			prop = PropertyFileReader.readPropertiesFile(Constants.propertyFilePath);
			l.info("Properties file loaded successfully.");
		} catch (IOException e) {
			l.error("Error loading properties file", e);
		}

		threadPool = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
		l.info("Thread Pool created");
		if (prop.getProperty("targetDB").equalsIgnoreCase("oracle")) {
			l.info("Target Database selected : " + prop.getProperty("targetDB"));
			if (prop.getProperty("autoGenerateMetadata").equalsIgnoreCase("yes")) {
				l.info("Auto Generated Metadata option selected as : " + prop.getProperty("autoGenerateMetadata"));
				MetadataExcelGenerator.generateMetadataExcel(Constants.mappingSheetPath_oracle,
						Constants.metaDataFilePath);
				metaDataExcelPath = Constants.metaDataFilePath;
				l.info("Metadata Excel Generation Completed....");
			} else {
				l.info("Auto Generated Metadata option selected as : " + prop.getProperty("autoGenerateMetadata"));
				metaDataExcelPath = Constants.metaDataFilePath_oracle;
				l.info("Metadata Excel Generation Completed....");
			}
			tableMetaDataQuery = Constants.metaDataQuery_oracle;
			jdbcUrl = prop.getProperty("jdbcUrl_oracle");
			username = prop.getProperty("username_oracle");
			password = prop.getProperty("password_oracle");
			try {
				Class.forName("oracle.jdbc.OracleDriver");
				targetConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Target database connection established successfully.");
			} catch (Exception e) {
				l.error("Error creating target database connection", e);
			}
		} else if (prop.getProperty("targetDB").equalsIgnoreCase("mysql")) {
			l.info("Target Database selected : " + prop.getProperty("targetDB"));

			if (prop.getProperty("autoGenerateMetadata").equalsIgnoreCase("yes")) {
				l.info("Auto Generated Metadata option selected as : " + prop.getProperty("autoGenerateMetadata"));
				MetadataExcelGenerator.generateMetadataExcel(Constants.mappingSheetPath_mysql,
						Constants.metaDataFilePath);
				metaDataExcelPath = Constants.metaDataFilePath;
				l.info("Metadata Excel Generation Completed....");
			} else {
				l.info("Auto Generated Metadata option selected as : " + prop.getProperty("autoGenerateMetadata"));
				metaDataExcelPath = Constants.metaDataFilePath_mysql;
				l.info("Metadata Excel Generation Completed....");
			}
			tableMetaDataQuery = Constants.metaDataQuery_mysql;
			jdbcUrl = prop.getProperty("jdbcUrl_mysql");
			username = prop.getProperty("username_mysql");
			password = prop.getProperty("password_mysql");
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				targetConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Target database connection established successfully.");

			} catch (Exception e) {
				l.error("Error creating target database connection", e);
			}
		}
		l.info("********************************    Target Set up completed     *********************************");
		if (prop.getProperty("sourceDB").equalsIgnoreCase("oracle")) {
			l.info("Source Database selected : " + prop.getProperty("sourceDB"));
			jdbcUrl = prop.getProperty("jdbcUrl_oracle");
			username = prop.getProperty("username_oracle");
			password = prop.getProperty("password_oracle");
			try {
				Class.forName("oracle.jdbc.OracleDriver");
				sourceConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Source database connection established successfully.");
			} catch (Exception e) {
				l.error("Error creating source database connection", e);
			}
		} else if (prop.getProperty("sourceDB").equalsIgnoreCase("mysql")) {
			l.info("Source Database selected : " + prop.getProperty("sourceDB"));
			jdbcUrl = prop.getProperty("jdbcUrl_mysql");
			username = prop.getProperty("username_mysql");
			password = prop.getProperty("password_mysql");
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				sourceConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Source database connection established successfully.");
			} catch (Exception e) {
				l.error("Error creating source database connection", e);
			}
		}
		l.info("********************************    Source Set up completed    *********************************");

	}

	@AfterClass
	public void tearDown() throws SQLException {
		l.info("Teardown started..............");
		if (workbook != null) {
			try {
				workbook.close();
				l.info("Workbook closed successfully.");
			} catch (IOException e) {
				l.error("Error closing workbook", e);
			}
		}
		if (sourceConnection != null) {
			DatabaseConn.closeConnection(sourceConnection);
			l.info("Source database connection closed successfully.");
		}
		if (targetConnection != null) {
			DatabaseConn.closeConnection(targetConnection);
			l.info("Target database connection closed successfully.");
		}

		// Shutdown the thread pool
		threadPool.shutdown();
		l.info("Thread pool shut down successfully.");
		l.info("****************************************************    Teardown Completed     ********************************************");
	}
}
