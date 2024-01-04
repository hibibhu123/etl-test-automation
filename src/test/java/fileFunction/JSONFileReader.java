package fileFunction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONFileReader {

	public static List<List<String>> readJSONFile(String jsonFilePath) {
		List<List<String>> outputList = new ArrayList<>();

		try {
			File jsonFile = new File(jsonFilePath);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(jsonFile);

			JsonNode itemsNode = jsonNode.at("/results/0/items");

			if (itemsNode.isArray()) {
				// Extract and display student details
				for (JsonNode item : itemsNode) {
					Iterator<String> fieldNames = item.fieldNames();
					List<String> rowData = new ArrayList<>();
					while (fieldNames.hasNext()) {
						String fieldName = fieldNames.next();
						String fieldValue = item.get(fieldName).asText();
						rowData.add(fieldName.toUpperCase() + ": " + fieldValue);
						
					}
					//outputList.add("[" + String.join(", ", rowData) + "]");
					outputList.add(rowData);

				}
			} else {
				System.err.println("Invalid JSON structure. Missing 'items' array.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputList;
	}

	public static void main(String[] args) {
		String jsonFilePath = "./student_small.json";
		System.out.println(readJSONFile(jsonFilePath));
	}
}
