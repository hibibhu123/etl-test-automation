package queryFunction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MetadataExcelGenerator {

    public static void generateMetadataExcel(String mappingSheetPath, String metadataExcelPath) {
        File metadataFile = new File(metadataExcelPath);

        // Check if the metadata file already exists
        if (metadataFile.exists()) {
            System.out.println("Metadata Excel is available at: " + metadataExcelPath+" "+"No need to generate from mapping sheet");
            return;
        }

        try {
            Workbook mappingWorkbook = WorkbookFactory.create(new File(mappingSheetPath));
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
                Row metadataTableHeaderRow = metadataTableSheet.createRow(0);
                metadataTableHeaderRow.createCell(0).setCellValue("COLUMN_NAME");
                metadataTableHeaderRow.createCell(1).setCellValue("DATA_TYPE");

                // Iterate through rows in the mapping sheet
                Iterator<Row> rowIterator = mappingSheet.iterator();
                // Skip the header row
                rowIterator.next();

                while (rowIterator.hasNext()) {
                    Row mappingRow = rowIterator.next();
                    String columnName = mappingRow.getCell(6).getStringCellValue(); // Assuming the column name is in the 7th column
                    String dataType = mappingRow.getCell(7).getStringCellValue(); // Assuming the data type is in the 8th column

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

            System.out.println("Metadata Excel generated successfully at: " + metadataExcelPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
