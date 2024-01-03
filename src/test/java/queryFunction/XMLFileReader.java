package queryFunction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLFileReader {

    public static List<List<String>> readXMLFile(String xmlFilePath) {
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList studentNodes = doc.getElementsByTagName("STUDENT");

            return convertToNestedList(studentNodes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<List<String>> convertToNestedList(NodeList studentNodes) {
        List<List<String>> result = new ArrayList<>();

        for (int i = 0; i < studentNodes.getLength(); i++) {
            Node studentNode = studentNodes.item(i);

            if (studentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element studentElement = (Element) studentNode;

                List<String> studentDetails = new ArrayList<>();
                NodeList columns = studentElement.getChildNodes();

                for (int j = 0; j < columns.getLength(); j++) {
                    Node columnNode = columns.item(j);

                    if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element columnElement = (Element) columnNode;
                        String columnName = columnElement.getTagName();
                        String columnValue = columnElement.getTextContent();
                        studentDetails.add(columnName + ": " + columnValue);
                    }
                }

                result.add(studentDetails);
            }
        }

        return result;
    }

    public static void main(String[] args) {
        String xmlFilePath = "./student_small.xml";
        List<List<String>> studentList = readXMLFile(xmlFilePath);

        
        System.out.println(studentList);
    }
}
