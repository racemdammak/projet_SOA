package cloud.logging;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OperationLogger {

    private static final String LOG_FILE = "operations_log.xml";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public enum OperationType {
        UPLOAD,
        TELECHARGER,
        SUPPRIMER,
        RESUMER;
    }

    public enum OperationStatus {
        SUCCESS, ERROR
    }
    public static void logOperation(OperationType type, String filename, OperationStatus status, String message, Long fileSize) {
        try {
            Document doc = getOrCreateDocument();
            Element root = doc.getDocumentElement();

            Element operation = doc.createElement("operation");

            String id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "_" + type;
            operation.setAttribute("id", id);

            Element timestamp = doc.createElement("timestamp");
            timestamp.setTextContent(LocalDateTime.now().format(DATE_FORMATTER));
            operation.appendChild(timestamp);

            Element typeElement = doc.createElement("type");
            typeElement.setTextContent(String.valueOf(type));
            operation.appendChild(typeElement);

            Element filenameElement = doc.createElement("filename");
            filenameElement.setTextContent(filename != null ? filename : "");
            operation.appendChild(filenameElement);

            Element statusElement = doc.createElement("status");
            statusElement.setTextContent(status.name());
            operation.appendChild(statusElement);

            Element messageElement = doc.createElement("message");
            messageElement.setTextContent(message != null ? message : "");
            operation.appendChild(messageElement);

            if (fileSize != null && fileSize > 0) {
                Element fileSizeElement = doc.createElement("fileSize");
                fileSizeElement.setTextContent(String.valueOf(fileSize));
                operation.appendChild(fileSizeElement);
            }

            root.appendChild(operation);

            saveDocument(doc);

        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'opération dans le log XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Document getOrCreateDocument() throws ParserConfigurationException, IOException, org.xml.sax.SAXException {
        File logFile = new File(LOG_FILE);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc;

        if (logFile.exists() && logFile.length() > 0) {
            doc = builder.parse(logFile);
        } else {
            doc = builder.newDocument();
            Element root = doc.createElement("operations");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xsi:noNamespaceSchemaLocation", "operations_log.xsd");
            doc.appendChild(root);
        }

        return doc;
    }
    private static void saveDocument(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(LOG_FILE));
        transformer.transform(source, result);
    }
}

