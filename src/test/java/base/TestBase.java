package base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import queryFunction.Database;
import queryFunction.DatabaseConn;
import queryFunction.MetadataExcelGenerator;
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
				targetConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Target database connection established successfully.");
			} catch (Exception e) {
				l.error("Error creating target database connection", e);
			}
		}

		if (prop.getProperty("sourceDB").equalsIgnoreCase("oracle")) {
			jdbcUrl = prop.getProperty("jdbcUrl_oracle");
			username = prop.getProperty("username_oracle");
			password = prop.getProperty("password_oracle");
			try {
				sourceConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Source database connection established successfully.");
			} catch (Exception e) {
				l.error("Error creating source database connection", e);
			}
		} else if (prop.getProperty("sourceDB").equalsIgnoreCase("mysql")) {
			jdbcUrl = prop.getProperty("jdbcUrl_mysql");
			username = prop.getProperty("username_mysql");
			password = prop.getProperty("password_mysql");
			try {
				sourceConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
				l.info("Source database connection established successfully.");
			} catch (Exception e) {
				l.error("Error creating source database connection", e);
			}
		}
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
		l.info("Teardown Completed..............");
	}
}
