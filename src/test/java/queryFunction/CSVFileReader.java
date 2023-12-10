package queryFunction;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class CSVFileReader {

    public static List<List<String>> readCSVFile(String filePath) throws IOException {
        List<List<String>> data = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] header = reader.readNext(); // Read the header row
            List<String> headerList = Arrays.asList(header);

            String[] line;
            while ((line = reader.readNext()) != null) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < line.length; i++) {
                    String columnName = headerList.get(i);
                    String columnValue = line[i];
                    row.add(columnName + ": " + columnValue);
                }
                data.add(row);
            }
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        return data;
    }
}
