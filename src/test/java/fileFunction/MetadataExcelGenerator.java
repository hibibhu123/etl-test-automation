package fileFunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger; // Import the Logger class
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MetadataExcelGenerator {

    private static final Logger l = Logger.getLogger(MetadataExcelGenerator.class); // Create a Logger instance

    public static void generateMetadataExcel(String mappingSheetPath, String metadataExcelPath) {
        l.info("Starting metadata Excel generation...");

        File metadataFile = new File(metadataExcelPath);

        // Check if the metadata file already exists
        if (metadataFile.exists()) {
            if (metadataFile.delete()) {
                l.info("Existing Metadata Excel deleted: " + metadataExcelPath);
            } else {
                l.warn("Failed to delete existing Metadata Excel: " + metadataExcelPath);
                return;
            }
        }

        try {
            Workbook mappingWorkbook;

            try (FileInputStream fis = new FileInputStream(new File(mappingSheetPath))) {
                if (mappingSheetPath.toLowerCase().endsWith(".xlsx")) {
                    mappingWorkbook = new XSSFWorkbook(fis);
                } else if (mappingSheetPath.toLowerCase().endsWith(".xls")) {
                    // For older Excel formats (HSSF)
                    mappingWorkbook = WorkbookFactory.create(fis);
                } else {
                    l.error("Unsupported Excel file format: " + mappingSheetPath);
                    return;
                }
            }

            Workbook metadataWorkbook = new XSSFWorkbook();

            // Create the first sheet in the metadata workbook
            Sheet metadataMainSheet = metadataWorkbook.createSheet("TABLE_NAMES");
            Row metadataHeaderRow = metadataMainSheet.createRow(0);
            metadataHeaderRow.createCell(0).setCellValue("TABLE_NAMES");

            int targetTableRowIndex = 1;

            // Iterate through the sheets in the mapping workbook
            for (int i = 0; i < mappingWorkbook.getNumberOfSheets(); i++) {
                Sheet mappingSheet = mappingWorkbook.getSheetAt(i);
                String targetTable = mappingSheet.getSheetName();

                // Add the target table to the Metadata sheet
                Row metadataTableRow = metadataMainSheet.createRow(targetTableRowIndex);
                metadataTableRow.createCell(0).setCellValue(targetTable);
                targetTableRowIndex++;

                // Create a new sheet for each target table in the metadata workbook
                Sheet metadataTableSheet = metadataWorkbook.createSheet(targetTable);

                // Find the indices dynamically
                int columnNameIndex = findColumnIndex(mappingSheet, "Target Column");
                int dataTypeIndex = findColumnIndex(mappingSheet, "Target Type");

                if (columnNameIndex == -1 || dataTypeIndex == -1) {
                    l.error("Could not find column name or data type headers in sheet: " + targetTable);
                    continue; // Skip this sheet and move on to the next one
                }

                Row metadataTableHeaderRow = metadataTableSheet.createRow(0);
                metadataTableHeaderRow.createCell(0).setCellValue("COLUMN_NAME");
                metadataTableHeaderRow.createCell(1).setCellValue("DATA_TYPE");

                // Iterate through rows in the mapping sheet
                Iterator<Row> rowIterator = mappingSheet.iterator();
                // Skip the header row
                rowIterator.next();

                while (rowIterator.hasNext()) {
                    Row mappingRow = rowIterator.next();
                    String columnName = mappingRow.getCell(columnNameIndex).getStringCellValue();
                    String dataType = mappingRow.getCell(dataTypeIndex).getStringCellValue();

                    // Add column information to the metadata table sheet
                    Row metadataTableRowInner = metadataTableSheet.createRow(metadataTableSheet.getLastRowNum() + 1);
                    metadataTableRowInner.createCell(0).setCellValue(columnName);
                    metadataTableRowInner.createCell(1).setCellValue(dataType);
                }
            }

            // Write the metadata workbook to a file
            try (FileOutputStream fileOut = new FileOutputStream(metadataExcelPath)) {
                metadataWorkbook.write(fileOut);
            }

            // Close the workbooks
            mappingWorkbook.close();
            metadataWorkbook.close();

            l.info("Metadata Excel auto-generated successfully at: " + metadataExcelPath);

        } catch (IOException e) {
            l.error("Error during metadata Excel generation: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static int findColumnIndex(Sheet sheet, String targetString) {
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow != null) {
            Iterator<Cell> cellIterator = headerRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getStringCellValue().toLowerCase().contains(targetString.toLowerCase())) {
                    return cell.getColumnIndex();
                }
            }
        }
        return -1; // Header not found
    }
}
