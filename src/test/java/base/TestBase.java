package base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import queryFunction.Database;
import queryFunction.DatabaseConn;
import util.Constants;
import util.CustomTestListener;
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

	@BeforeClass
	public void setUp() {
		try {
			prop = PropertyFileReader.readPropertiesFile(Constants.propertyFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (prop.getProperty("targetDB").equalsIgnoreCase("oracle")) {
			metaDataExcelPath = Constants.metaDataFilePath_oracle;
			tableMetaDataQuery = Constants.metaDataQuery_oracle;
			jdbcUrl = prop.getProperty("jdbcUrl_oracle");
			username = prop.getProperty("username_oracle");
			password = prop.getProperty("password_oracle");
			try {
				targetConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (prop.getProperty("targetDB").equalsIgnoreCase("mysql")) {
			metaDataExcelPath = Constants.metaDataFilePath_mysql;
			tableMetaDataQuery = Constants.metaDataQuery_mysql;
			jdbcUrl = prop.getProperty("jdbcUrl_mysql");
			username = prop.getProperty("username_mysql");
			password = prop.getProperty("password_mysql");
			try {
				targetConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (prop.getProperty("sourceDB").equalsIgnoreCase("oracle")) {
			jdbcUrl = prop.getProperty("jdbcUrl_oracle");
			username = prop.getProperty("username_oracle");
			password = prop.getProperty("password_oracle");
			try {
				sourceConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (prop.getProperty("sourceDB").equalsIgnoreCase("mysql")) {
			jdbcUrl = prop.getProperty("jdbcUrl_mysql");
			username = prop.getProperty("username_mysql");
			password = prop.getProperty("password_mysql");
			try {
				sourceConnection = DatabaseConn.createConnection(jdbcUrl, username, password);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@AfterClass
	public void tearDown() throws SQLException {
		if (workbook != null) {
			try {
				workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (sourceConnection != null) {
			DatabaseConn.closeConnection(sourceConnection);
		}
		if (targetConnection != null) {
			DatabaseConn.closeConnection(targetConnection);
		}
	}

}
