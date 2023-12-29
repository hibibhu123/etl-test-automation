package testCases;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.TestBase;
import queryFunction.DatabaseMetadataRetrieval;
import queryFunction.MySqlMetadataRetrieval;
import queryFunction.OracleMetadataRetrieval;

public class TableMetadataValidation extends TestBase {

	@Test(dataProvider = "tableSheetDataProvider")
	public void tableMetadataValidation(String tableName) {
		try {
			testCaseCounter++;
			l.info("#########################################################################    Test Case "
					+ testCaseCounter + " : Table Metadata Validation for " + tableName + " : STARTED");
			l.info("Processing Table: " + tableName);

			DatabaseMetadataRetrieval metadataRetrieval = getMetadataRetrievalForCurrentDatabase(prop);

			List<Map<String, Object>> sourceMetadata = getTableMetadata(workbook, tableName);
			List<Map<String, Object>> targetMetadata = metadataRetrieval.getTableMetadata(tableName, targetConnection);

			boolean isMetadataEqual = compareMetadata(sourceMetadata, targetMetadata, metadataRetrieval, tableName);

			if (isMetadataEqual) {

				l.info("Metadata Match for Table: " + tableName);
				l.info("#########################################################################    Test Case "
						+ testCaseCounter + " : Metadata Validation for " + tableName + " : PASSED");
				testResults.add("Test Case " + testCaseCounter +" Metadata Validtion for : "+tableName+ ": PASSED");

			} else {

				l.error("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    Test Case "
						+ testCaseCounter + " : Table Metadata Validation for " + tableName + " : FAILED");

				testResults.add("Test Case " + testCaseCounter +" Metadata Validtion for : "+tableName+ ": FAILED");
				try {
					Assert.assertTrue(isMetadataEqual, TableMetadataValidation.findMismatchedColumns(sourceMetadata,
							targetMetadata, metadataRetrieval));
				} catch (AssertionError e) {
					l.error("Test failed: " + e.getMessage(), e);
					throw new AssertionError("Test failed: " + e.getMessage(), e);
				}

			}
		}

		catch (SQLException sqlException) {
			// SQL syntax error detected
			String errorMessage = "SQL Syntax Error: " + sqlException.getMessage();
			// Log the "FAILED" message for other exceptions
			l.error(errorMessage, sqlException);
			l.error("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    Test Case "
					+ testCaseCounter + " : Table Metadata Validation for " + tableName + " : FAILED");
			testResults.add("Test Case " + testCaseCounter +" Metadata Validtion for : "+tableName+ ": FAILED");
			throw new AssertionError(errorMessage, sqlException);
		} catch (Exception e) {
			l.error("Test failed: " + e.getMessage(), e);
			l.error("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$    Test Case "
					+ testCaseCounter + " : Table Metadata Validation for " + tableName + " : FAILED");
			testResults.add("Test Case " + testCaseCounter +" Metadata Validtion for : "+tableName+ ": FAILED");
			throw new AssertionError("Test failed: " + e.getMessage(), e);
		}
	}

	@DataProvider(name = "tableSheetDataProvider")
	public Iterator<Object[]> tableSheetDataProvider() {
		List<Object[]> data = new ArrayList<>();

		try {
			workbook = new XSSFWorkbook(metaDataExcelPath);
			l.info("Workbook loaded successfully.");

			List<String> tableNames = getTableNames(workbook);

			for (String tableName : tableNames) {
				data.add(new Object[] { tableName });
			}
		} catch (IOException e) {
			l.error("Error loading workbook: " + e.getMessage(), e);
		}

		return data.iterator();
	}

	private DatabaseMetadataRetrieval getMetadataRetrievalForCurrentDatabase(Properties prop) {
		String databaseType = prop.getProperty("targetDB");

		switch (databaseType.toLowerCase()) {
		case "mysql":
			return new MySqlMetadataRetrieval();
		case "oracle":
			return new OracleMetadataRetrieval();
		default:
			throw new IllegalArgumentException("Unsupported database type: " + databaseType);
		}
	}

	private List<String> getTableNames(XSSFWorkbook workbook) {
		List<String> tableNames = new ArrayList<>();
		Sheet sheet = workbook.getSheetAt(0);

		Iterator<Row> iterator = sheet.iterator();
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
				l.warn("Unexpected cell format in table names, Row: " + currentRow.getRowNum());
			}
		}

		return tableNames;
	}

	private List<Map<String, Object>> getTableMetadata(XSSFWorkbook workbook, String tableName) {
		List<Map<String, Object>> metadataList = new ArrayList<>();
		Sheet sheet = workbook.getSheet(tableName);

		if (sheet == null) {
			l.warn("Sheet not found for table: " + tableName);
			return metadataList;
		}

		Iterator<Row> iterator = sheet.iterator();
		if (iterator.hasNext()) {
			iterator.next(); // Skip the header row
		}

		while (iterator.hasNext()) {
			Row currentRow = iterator.next();
			Cell columnNameCell = currentRow.getCell(0);
			Cell dataLengthCell = currentRow.getCell(1);

			if (columnNameCell != null && columnNameCell.getCellType() == CellType.STRING && dataLengthCell != null
					&& dataLengthCell.getCellType() == CellType.STRING) {

				Map<String, Object> columnMetadata = new TreeMap<>();
				columnMetadata.put("COLUMN_NAME", columnNameCell.getStringCellValue());

				switch (dataLengthCell.getCellType()) {
				case NUMERIC:
					columnMetadata.put("DATA_TYPE", String.valueOf((int) dataLengthCell.getNumericCellValue()));
					break;
				case STRING:
					columnMetadata.put("DATA_TYPE", dataLengthCell.getStringCellValue().trim());
					break;
				default:
					columnMetadata.put("DATA_TYPE", "");
					break;
				}

				metadataList.add(columnMetadata);
			} else {
				l.warn("Unexpected cell format for table: " + tableName + ", Row: " + currentRow.getRowNum());
			}
		}

		// Sort metadataList based on COLUMN_NAME
		Collections.sort(metadataList, (a, b) -> String.valueOf(a.get("COLUMN_NAME"))
				.compareToIgnoreCase(String.valueOf(b.get("COLUMN_NAME"))));

		l.info("Sorted Metadata from Mapping Sheet:" + metadataList);
		return metadataList;
	}

	private boolean compareMetadata(List<Map<String, Object>> sourceMetadata, List<Map<String, Object>> targetMetadata,
			DatabaseMetadataRetrieval metadataRetrieval, String tableName) {
		if (sourceMetadata.size() != targetMetadata.size()) {
			l.error("Metadata size mismatch for table: " + tableName);
			return false;
		}

		for (int i = 0; i < sourceMetadata.size(); i++) {
			Map<String, Object> sourceColumn = sourceMetadata.get(i);
			Map<String, Object> targetColumn = targetMetadata.get(i);

			if (!isEqual(sourceColumn, targetColumn, metadataRetrieval)) {
				l.info("Metadata Mismatch for Table: " + tableName);
				l.info("Source Column: " + sourceColumn);
				l.info("Target Column: " + targetColumn);
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

	private static String findMismatchedColumns(List<Map<String, Object>> sourceMetadata,
			List<Map<String, Object>> targetMetadata, DatabaseMetadataRetrieval metadataRetrieval) {

		List<Map<String, Object>> mismatchedColumns = new ArrayList<>();

		for (int i = 0; i < sourceMetadata.size(); i++) {
			Map<String, Object> sourceColumn = sourceMetadata.get(i);
			Map<String, Object> targetColumn = targetMetadata.get(i);

			if (!isEqual(sourceColumn, targetColumn, metadataRetrieval)) {
				Map<String, Object> mismatchedColumn = new TreeMap<>();
				mismatchedColumn.put("COLUMN_NAME", sourceColumn.get("COLUMN_NAME"));
				mismatchedColumn.put("TARGET_COLUMN_NAME", targetColumn.get("COLUMN_NAME"));
				mismatchedColumns.add(mismatchedColumn);
			}
		}

		return mismatchedColumns.toString();
	}

}
