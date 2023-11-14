package testCases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.Test;

import base.TestBase;
import queryFunction.DatabaseMetadataRetrieval;
import queryFunction.MySqlMetadataRetrieval;
import queryFunction.OracleMetadataRetrieval;

public class MetadataValidation extends TestBase {

	@Test
	public void testMetadataComparison() {
		try {
			// Load workbook and establish JDBC connection before all tests
			workbook = new XSSFWorkbook(metaDataExcelPath);
			List<String> tableNames = getTableNames(workbook);

			for (String tableName : tableNames) {
				System.out.println("Processing Table: " + tableName);

				// Update this line to get the appropriate implementation based on your
				// configuration
				DatabaseMetadataRetrieval metadataRetrieval = getMetadataRetrievalForCurrentDatabase(prop);

				List<Map<String, Object>> sourceMetadata = getTableMetadata(workbook, tableName);
				List<Map<String, Object>> targetMetadata = metadataRetrieval.getTableMetadata(tableName,
						targetConnection);

				// Compare source and target metadata
				boolean isMetadataEqual = compareMetadata(sourceMetadata, targetMetadata, metadataRetrieval);

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

	private static DatabaseMetadataRetrieval getMetadataRetrievalForCurrentDatabase(Properties prop) {
		String databaseType = prop.getProperty("targetDB");

		switch (databaseType.toLowerCase()) {
		case "mysql":
			return new MySqlMetadataRetrieval();
		case "oracle":
			return new OracleMetadataRetrieval();
		// Add more cases for other databases as needed
		default:
			throw new IllegalArgumentException("Unsupported database type: " + databaseType);
		}
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

				// Check the cell type for DATA_LENGTH
				if (dataLengthCell != null) {
					switch (dataLengthCell.getCellType()) {
					case NUMERIC:
						// If it's a numeric cell, get the numeric value
						columnMetadata.put("DATA_LENGTH", String.valueOf((int) dataLengthCell.getNumericCellValue()));
						break;
					case STRING:
						// If it's a string cell, get the string value
						columnMetadata.put("DATA_LENGTH", dataLengthCell.getStringCellValue().trim());
						break;
					default:
						// Handle other cell types if needed
						columnMetadata.put("DATA_LENGTH", "");
						break;
					}
				} else {
					columnMetadata.put("DATA_LENGTH", "");
				}

				metadataList.add(columnMetadata);
			} else {
				System.out
						.println("Unexpected cell format for table: " + tableName + ", Row: " + currentRow.getRowNum());
			}

		}
		System.out.println("Metadata from Mapping Sheet:" + metadataList);
		return metadataList;
	}

	private static boolean compareMetadata(List<Map<String, Object>> sourceMetadata,
			List<Map<String, Object>> targetMetadata, DatabaseMetadataRetrieval metadataRetrieval) {
		// Compare the structure (number of columns)
		if (sourceMetadata.size() != targetMetadata.size()) {
			return false;
		}

		for (int i = 0; i < sourceMetadata.size(); i++) {
			Map<String, Object> sourceColumn = sourceMetadata.get(i);
			Map<String, Object> targetColumn = targetMetadata.get(i);

			// Pass metadataRetrieval to isEqual method
			if (!isEqual(sourceColumn, targetColumn, metadataRetrieval)) {
				System.out.println("Mismatch details:");
				System.out.println("Source Column: " + sourceColumn);
				System.out.println("Target Column: " + targetColumn);
				return false;
			}
		}

		return true;
	}

	private static boolean isEqual(Map<String, Object> sourceColumn, Map<String, Object> targetColumn,
			DatabaseMetadataRetrieval databaseMetadataRetrieval) {
		// Use the provided database-specific logic for metadata comparison
		return databaseMetadataRetrieval.isEqual(sourceColumn, targetColumn);
	}

}
