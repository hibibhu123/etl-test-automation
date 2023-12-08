package testCases;

import java.io.IOException;
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
			System.out.println("Processing Table: " + tableName);

			DatabaseMetadataRetrieval metadataRetrieval = getMetadataRetrievalForCurrentDatabase(prop);

			List<Map<String, Object>> sourceMetadata = getTableMetadata(workbook, tableName);
			List<Map<String, Object>> targetMetadata = metadataRetrieval.getTableMetadata(tableName, targetConnection);

			boolean isMetadataEqual = compareMetadata(sourceMetadata, targetMetadata, metadataRetrieval);

			if (isMetadataEqual) {
				System.out.println("Metadata Match for Table: " + tableName);
			} else {
				System.out.println("Metadata Mismatch for Table: " + tableName);
				Assert.fail("Metadata Mismatch for Table: " + tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An exception occurred: " + e.getMessage());
		}
	}

	@DataProvider(name = "tableSheetDataProvider")
	public Iterator<Object[]> tableSheetDataProvider() {
		List<Object[]> data = new ArrayList<>();

		try {
			workbook = new XSSFWorkbook(metaDataExcelPath);

			List<String> tableNames = getTableNames(workbook);

			for (String tableName : tableNames) {
				data.add(new Object[] { tableName });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data.iterator();
	}

	private static DatabaseMetadataRetrieval getMetadataRetrievalForCurrentDatabase(Properties prop) {
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

	private static List<String> getTableNames(XSSFWorkbook workbook) {
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
	            System.out.println("Unexpected cell format for table: " + tableName + ", Row: " + currentRow.getRowNum());
	        }
	    }

	    // Sort metadataList based on COLUMN_NAME
	    Collections.sort(metadataList, (a, b) ->
        String.valueOf(a.get("COLUMN_NAME")).compareToIgnoreCase(String.valueOf(b.get("COLUMN_NAME"))));

System.out.println("Sorted Metadata from Mapping Sheet:" + metadataList);
return metadataList;
	}


	private static boolean compareMetadata(List<Map<String, Object>> sourceMetadata,
			List<Map<String, Object>> targetMetadata, DatabaseMetadataRetrieval metadataRetrieval) {
		if (sourceMetadata.size() != targetMetadata.size()) {
			return false;
		}

		for (int i = 0; i < sourceMetadata.size(); i++) {
			Map<String, Object> sourceColumn = sourceMetadata.get(i);
			Map<String, Object> targetColumn = targetMetadata.get(i);

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
