package testCases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TableMetadataComparator {

	private static final String JDBC_URL = "jdbc:oracle:thin:@//localhost:1521/orcl";
	private static final String USERNAME = "scott";
	private static final String PASSWORD = "tiger";
	private XSSFWorkbook workbook;
	private Connection connection;

	@BeforeClass
	public void setUp() throws Exception {
		
		
		// Load workbook and establish JDBC connection before all tests
		String excelFilePath = "./TableMetadata/Metadata.xlsx";
		workbook = new XSSFWorkbook(excelFilePath);
		connection = getConnection();
	}

	@AfterClass
	public void tearDown() throws Exception {
		// Close workbook and JDBC connection after all tests
		if (workbook != null) {
			workbook.close();
		}
		if (connection != null) {
			connection.close();
		}
	}

	@Test
	public void testMetadataComparison() {
		try {
			List<String> tableNames = getTableNames(workbook);

			for (String tableName : tableNames) {
				System.out.println("Processing Table: " + tableName);
				List<ColumnMetadata> sourceMetadata = getTableMetadata(workbook, tableName);
				List<ColumnMetadata> targetMetadata = getTableMetadataFromDatabase(tableName, connection);

				// Compare source and target metadata
				boolean isMetadataEqual = compareMetadata(sourceMetadata, targetMetadata);

				if (isMetadataEqual) {
					System.out.println("Metadata Match for Table: " + tableName);
				} else {
					System.out.println("Metadata Mismatch for Table: " + tableName);
					Assert.fail("Metadata Mismatch for Table: " + tableName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An exception occurred: " + e.getMessage());
		}
	}

	private static Connection getConnection() {
		Connection connection = null;

		try {
			// Register the Oracle JDBC driver
			Class.forName("oracle.jdbc.driver.OracleDriver");

			// Create properties for the connection
			Properties properties = new Properties();
			properties.setProperty("user", USERNAME);
			properties.setProperty("password", PASSWORD);

			// Establish the connection
			connection = DriverManager.getConnection(JDBC_URL, properties);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return connection;
	}

	private static List<ColumnMetadata> getTableMetadataFromDatabase(String tableName, Connection connection) {
		List<ColumnMetadata> metadataList = new ArrayList<>();

		try {
			// Build the custom query
			String query = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH " + "FROM ALL_TAB_COLUMNS "
					+ "WHERE TABLE_NAME = '" + tableName + "'";

			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				String columnName = resultSet.getString("COLUMN_NAME");
				String dataType = resultSet.getString("DATA_TYPE");
				int dataLength = resultSet.getInt("DATA_LENGTH");

				ColumnMetadata columnMetadata = new ColumnMetadata(columnName, dataType, dataLength);
				metadataList.add(columnMetadata);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println(metadataList);
		return metadataList;
	}

	private static List<String> getTableNames(XSSFWorkbook workbook) {
		List<String> tableNames = new ArrayList<>();
		Sheet sheet = workbook.getSheetAt(0); // Assuming table names are in the first sheet

		Iterator<Row> iterator = sheet.iterator();
		// Skip the header row
		if (iterator.hasNext()) {
			iterator.next();
		}

		while (iterator.hasNext()) {
			Row currentRow = iterator.next();
			Cell tableNameCell = currentRow.getCell(0);

			if (tableNameCell != null && tableNameCell.getCellType() == CellType.STRING) {
				String tableName = tableNameCell.getStringCellValue();
				tableNames.add(tableName);
			} else {
				System.out.println("Unexpected cell format in table names, Row: " + currentRow.getRowNum());
			}
		}

		return tableNames;
	}

	private static List<ColumnMetadata> getTableMetadata(XSSFWorkbook workbook, String tableName) {
		List<ColumnMetadata> metadataList = new ArrayList<>();
		Sheet sheet = workbook.getSheet(tableName);

		if (sheet == null) {
			System.out.println("Sheet not found for table: " + tableName);
			return metadataList;
		}

		Iterator<Row> iterator = sheet.iterator();
		// Skip the header row
		if (iterator.hasNext()) {
			iterator.next();
		}

		while (iterator.hasNext()) {
			Row currentRow = iterator.next();
			Cell columnNameCell = currentRow.getCell(0);
			Cell dataTypeCell = currentRow.getCell(1);
			Cell dataLengthCell = currentRow.getCell(2);

			if (columnNameCell != null && columnNameCell.getCellType() == CellType.STRING && dataTypeCell != null
					&& dataTypeCell.getCellType() == CellType.STRING) {

				String columnName = columnNameCell.getStringCellValue();
				String dataType = dataTypeCell.getStringCellValue();
				int dataLength = 0; // Default to 0 if the cell is empty

				if (dataLengthCell != null && dataLengthCell.getCellType() == CellType.NUMERIC) {
					dataLength = (int) dataLengthCell.getNumericCellValue();
				}

				ColumnMetadata columnMetadata = new ColumnMetadata(columnName, dataType, dataLength);
				metadataList.add(columnMetadata);
			} else {
				System.out
						.println("Unexpected cell format for table: " + tableName + ", Row: " + currentRow.getRowNum());
			}
		}
		System.out.println(metadataList);
		return metadataList;
	}

	private static boolean compareMetadata(List<ColumnMetadata> sourceMetadata, List<ColumnMetadata> targetMetadata) {
		// Compare the structure (number of columns)
		if (sourceMetadata.size() != targetMetadata.size()) {
			return false;
		}

		for (int i = 0; i < sourceMetadata.size(); i++) {
			ColumnMetadata sourceColumn = sourceMetadata.get(i);
			ColumnMetadata targetColumn = targetMetadata.get(i);

			// Compare each element within the row, excluding DATA_LENGTH for DATE data type
			if (!isEqual(sourceColumn, targetColumn)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isEqual(ColumnMetadata sourceColumn, ColumnMetadata targetColumn) {
		// Compare ignoring leading/trailing whitespaces
		boolean columnNameMatch = sourceColumn.getColumnName().trim().equals(targetColumn.getColumnName().trim());
		boolean dataTypeMatch = sourceColumn.getDataType().trim().equals(targetColumn.getDataType().trim());
		boolean dataLengthMatch = sourceColumn.getDataType().equalsIgnoreCase("DATE")
				|| sourceColumn.getDataLength() == targetColumn.getDataLength();

		if (!columnNameMatch || !dataTypeMatch || !dataLengthMatch) {
			String errorMessage = "Mismatch in column: " + sourceColumn.getColumnName() + "\nSource: " + sourceColumn
					+ "\nTarget: " + targetColumn;

			Assert.fail(errorMessage);
		}

		return true;
	}

	private static class ColumnMetadata {
		private String columnName;
		private String dataType;
		private int dataLength;

		public ColumnMetadata(String columnName, String dataType, int dataLength) {
			this.columnName = columnName;
			this.dataType = dataType;
			this.dataLength = dataLength;
		}

		// Getters and setters (if needed)

		public String getColumnName() {
			return columnName;
		}

		public String getDataType() {
			return dataType;
		}

		public int getDataLength() {
			return dataLength;
		}

		@Override
		public String toString() {
			return "ColumnMetadata{" + "columnName='" + columnName + '\'' + ", dataType='" + dataType + '\''
					+ ", dataLength=" + dataLength + '}';
		}
	}
}
