package testCases;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.TestBase;

public class MetadataValidation extends TestBase {

	@Test
	public void testMetadataComparison() {
		try {
			// Load workbook and establish JDBC connection before all tests
			workbook = new XSSFWorkbook(metaDataExcelPath);
			List<String> tableNames = getTableNames(workbook);

			for (String tableName : tableNames) {
				System.out.println("Processing Table: " + tableName);
				List<Map<String, Object>> sourceMetadata = getTableMetadata(workbook, tableName);
				List<Map<String, Object>> targetMetadata = getTableMetadataFrom(tableName, targetConnection);

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

	private static List<Map<String, Object>> getTableMetadataFrom(String tableName, Connection connection) {
		List<Map<String, Object>> metadataList = new ArrayList<>();

		try {
			// Build the custom query
			String query = tableMetaDataQuery + "'" + tableName + "'";

			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				Map<String, Object> columnMetadata = new HashMap<>();
				columnMetadata.put("COLUMN_NAME", resultSet.getString("COLUMN_NAME"));
				columnMetadata.put("DATA_TYPE", resultSet.getString("DATA_TYPE"));
				columnMetadata.put("COLUMN_TYPE", resultSet.getString("COLUMN_TYPE"));

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

	private static List<Map<String, Object>> getTableMetadata(XSSFWorkbook workbook, String tableName) {
		List<Map<String, Object>> metadataList = new ArrayList<>();
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

				Map<String, Object> columnMetadata = new HashMap<>();
				columnMetadata.put("COLUMN_NAME", columnNameCell.getStringCellValue());
				columnMetadata.put("DATA_TYPE", dataTypeCell.getStringCellValue());

				// Convert DATA_LENGTH to String
				String dataLength = "";
				if (dataLengthCell != null) {
					dataLength = dataLengthCell.toString();
				}
				columnMetadata.put("COLUMN_TYPE", dataLength);

				metadataList.add(columnMetadata);
			} else {
				System.out
						.println("Unexpected cell format for table: " + tableName + ", Row: " + currentRow.getRowNum());
			}
		}
		System.out.println(metadataList);
		return metadataList;
	}

	private static boolean compareMetadata(List<Map<String, Object>> sourceMetadata,
			List<Map<String, Object>> targetMetadata) {
		// Compare the structure (number of columns)
		if (sourceMetadata.size() != targetMetadata.size()) {
			return false;
		}

		for (int i = 0; i < sourceMetadata.size(); i++) {
			Map<String, Object> sourceColumn = sourceMetadata.get(i);
			Map<String, Object> targetColumn = targetMetadata.get(i);

			// Compare each element within the row, excluding DATA_LENGTH for DATE data type
			if (!isEqual(sourceColumn, targetColumn)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn) {
		// Compare ignoring leading/trailing whitespaces
		boolean columnNameMatch = sourceColumn.get("COLUMN_NAME").toString().trim()
				.equals(targetColumn.get("COLUMN_NAME").toString().trim());
		boolean dataTypeMatch = sourceColumn.get("DATA_TYPE").toString().trim()
				.equals(targetColumn.get("DATA_TYPE").toString().trim());
		boolean dataLengthMatch = sourceColumn.get("DATA_TYPE").toString().equalsIgnoreCase("DATE")
				|| sourceColumn.get("COLUMN_TYPE").toString().equals(targetColumn.get("COLUMN_TYPE").toString());

		if (!columnNameMatch || !dataTypeMatch || !dataLengthMatch) {
			String errorMessage = "Mismatch in column: " + sourceColumn.get("COLUMN_NAME") + "\nSource: " + sourceColumn
					+ "\nTarget: " + targetColumn;

			Assert.fail(errorMessage);
		}

		return true;
	}
}
