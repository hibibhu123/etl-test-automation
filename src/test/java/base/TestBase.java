package base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import queryFunction.DatabaseConn;
import util.Constants;
import util.PropertyFileReader;

public class TestBase {

	public Connection connection;
	public Properties prop;
	public XSSFWorkbook workbook;
	public String jdbcUrl;
	public String username;
	public String password;

	@BeforeMethod
	public void setUp() {
		try {
			prop = PropertyFileReader.readPropertiesFile(Constants.propertyFilePath);
		} catch (IOException e) {

			e.printStackTrace();
		}
		jdbcUrl = prop.getProperty("jdbcUrl");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		try {
			connection = DatabaseConn.createConnection(jdbcUrl, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterMethod
	public void tearDown() throws SQLException {
		
		 if (workbook != null) { 
			 try {
				workbook.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		 }
		if (connection != null) {
			DatabaseConn.closeConnection(connection);
		}
	}

}
