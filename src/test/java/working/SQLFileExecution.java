package working;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLFileExecution {

    public static void main(String[] args) {
        // JDBC URL, username, and password of your database
		String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/orcl";
		String username = "scott";
		String password = "tiger";

        // SQL script file path
        String scriptFilePath = "./testcases/source/source.sql";

        // Execute the script
        executeScript(jdbcUrl, username, password, scriptFilePath);
    }

    private static void executeScript(String jdbcUrl, String username, String password, String scriptFilePath) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(scriptFilePath))) {

            String line;
            StringBuilder scriptContent = new StringBuilder();

            // Read the SQL script file
            while ((line = reader.readLine()) != null) {
                scriptContent.append(line).append("\n");
            }

            // Execute the script
            statement.execute(scriptContent.toString());

            System.out.println("Script executed successfully!");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}

