package util;

import org.apache.log4j.Logger;

public class Banner {



	    private static final Logger l = Logger.getLogger(Banner.class);

	    public static void printLargeResultTextToLog() {
	        String text = "RESULT";
	        String[] lines = new String[5];

	        for (int i = 0; i < 5; i++) {
	            StringBuilder lineBuilder = new StringBuilder();
	            for (int j = 0; j < text.length(); j++) {
	                char currentChar = text.charAt(j);
	                String[] charLines = getLargeTextLines(currentChar);
	                lineBuilder.append(charLines[i]).append("  ");
	            }
	            lines[i] = lineBuilder.toString();
	        }

	        for (String line : lines) {
	            l.info(line);
	        }
	    }
    private static String[] getLargeTextLines(char ch) {
        switch (ch) {
            case 'R':
                return new String[]{"*****", "*   *", "*****", "*  **", "*   **"};
            case 'E':
                return new String[]{"   *****", "   *    ", "   ****", "   *    ", "  *****"};
            case 'S':
                return new String[]{"  ****  ", "  *     ", "   ****  ", "     * ", "  **** "};
            case 'U':
                return new String[]{"*   *", "*   *", "*   *", " *   *", "  *** "};
            case 'L':
                return new String[]{"  *    ", "  *    ", "  *    ", "  *    ", "  *****"};
            case 'T':
                return new String[]{"*****", "  *  ", "  *  ", "  *  ", "  *  "};
            default:
                // For unsupported characters, return an array of spaces
                return new String[]{"     ", "     ", "     ", "     ", "     "};
        }
    }

    public static void main(String[] args) {

        printLargeResultTextToLog();
    }
}
