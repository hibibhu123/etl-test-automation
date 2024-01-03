package queryFunction;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.log4j.Logger; // Import the Logger class

public class CSVFileReader {

	private static final Logger l = Logger.getLogger(CSVFileReader.class); // Create a Logger instance

	public static List<List<String>> readCSVFile(String filePath) throws IOException {
		List<List<String>> data = new ArrayList<>();

		try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
			String[] header = reader.readNext(); // Read the header row
			List<String> headerList = Arrays.asList(header);

			l.info("Reading CSV file: " + filePath);

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

			l.info("CSV file read successfully. Number of rows: " + data.size());
		} catch (CsvValidationException e) {
			l.error("Error reading CSV file: " + e.getMessage(), e);
			e.printStackTrace();
		}
		System.out.println(data);
		return data;
	}

	public static void main(String[] args) throws IOException {
		String jsonFilePath = "./student_small.csv";
		readCSVFile(jsonFilePath);
	}
}
