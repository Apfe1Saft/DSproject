package org.example.server.manager;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class DBManager {
    private static final String XML_FILE = "XMLDB.xml";
    private static final Logger logger = Logger.getLogger(DBManager.class);

    public DBManager() {
        initializeXML();
    }

    private static void initializeXML() {
        logger.info("initializeXML - XML DB EXISTENCE CHECK.");
        File file = new File(XML_FILE);
        if (!file.exists() || file.length() == 0) {
            logger.info("DB DOES NOT EXIST. CREATING DB FILE.");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Notebook></Notebook>");
                logger.info("DB FILE CREATED SUCCESSFULLY.");
            } catch (IOException e) {
                logger.error("ERROR WITH initializeXML", e);
            }
        } else {
            logger.info("XML FILE IS EXIST AND NOT EMPTY.");
        }
    }

    private synchronized static void saveXML(Document doc) throws TransformerException {
        logger.info("saveXML - CHANGES SAVE.");
        doc.getDocumentElement().normalize();
        removeWhitespaceNodes(doc);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);

        try (FileOutputStream fos = new FileOutputStream(XML_FILE, false);
             FileChannel channel = fos.getChannel();
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            channel.force(true);
            logger.info("XML FILE SAVED SUCCESSFULLY WITH CHANGES.");
        } catch (IOException e) {
            logger.error("ERROR WITH saveXML.", e);
        }
    }

    private synchronized static void removeWhitespaceNodes(Node node) {
        logger.info("removeWhitespaceNodes - WORK WITH WHITESPACES.");
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                logger.info("EMPTY TEXT NODE REMOVING.");
                node.removeChild(child);
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeWhitespaceNodes(child);
            }
        }
    }

    public synchronized String addNote(String topic, String text, String timestamp) {
        logger.info("addNote - ADD NOTE TO TOPIC: " + topic);
        try {
            File file = new File(XML_FILE);
            if (!file.exists()) {
                logger.info("XML FILE DOES NOT EXIST.");
                initializeXML();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            Element root = doc.getDocumentElement();
            NodeList topics = root.getElementsByTagName("Topic");
            Element topicElement = null;

            for (int i = 0; i < topics.getLength(); i++) {
                Element t = (Element) topics.item(i);
                if (t.getAttribute("name").equals(topic)) {
                    topicElement = t;
                    logger.info("TOPIC FOUND: " + topic);
                    break;
                }
            }

            if (topicElement == null) {
                topicElement = doc.createElement("Topic");
                topicElement.setAttribute("name", topic);
                root.appendChild(topicElement);
                logger.info("TOPIC CREATED: " + topic);
            }

            Element note = doc.createElement("Note");
            note.setAttribute("timestamp", timestamp);
            note.setTextContent(text);
            topicElement.appendChild(note);
            logger.info("NEW NOTE ADDED TO TOPIC: " + topic + " AT: " + timestamp);

            saveXML(doc);
            return "Note added successfully.";
        } catch (Exception e) {
            logger.error("ERROR WITH addNote:" + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    public synchronized String getNotes(String topic) {
        logger.info("getNotes - GET NOTE FROM TOPIC: " + topic);
        try {
            initializeXML();

            File file = new File(XML_FILE);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList topics = doc.getElementsByTagName("Topic");
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < topics.getLength(); i++) {
                Element t = (Element) topics.item(i);
                if (t.getAttribute("name").equals(topic)) {
                    logger.info("TOPIC FOUND: " + topic);
                    NodeList notes = t.getElementsByTagName("Note");
                    for (int j = 0; j < notes.getLength(); j++) {
                        Element note = (Element) notes.item(j);
                        result.append(note.getAttribute("timestamp")).append(" - ").append(note.getTextContent()).append("\n");
                    }
                    return result.length() > 0 ? result.toString() : "No notes found for this topic.";
                }
            }
            return "Topic not found.";
        } catch (Exception e) {
            logger.error("ERROR WITH getNotes: " + topic, e);
            return "Error: " + e.getMessage();
        }
    }

    public synchronized String deleteNote(String topic, String timestamp) {
        logger.info("deleteNote - DELETE NOTE: " + timestamp + " for topic: " + topic);
        try {
            initializeXML();

            File file = new File(XML_FILE);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList topics = doc.getElementsByTagName("Topic");
            for (int i = 0; i < topics.getLength(); i++) {
                Element t = (Element) topics.item(i);
                if (t.getAttribute("name").equals(topic)) {
                    NodeList notes = t.getElementsByTagName("Note");
                    for (int j = 0; j < notes.getLength(); j++) {
                        Element note = (Element) notes.item(j);
                        if (note.getAttribute("timestamp").equals(timestamp)) {
                            t.removeChild(note);
                            logger.info("NOTE WITH TIMESTAMP: " + timestamp + " DELETED SUCCESSFULLY.");
                            saveXML(doc);
                            return "Note deleted successfully.";
                        }
                    }
                    return "Note not found.";
                }
            }
            return "Topic not found.";
        } catch (Exception e) {
            logger.error("ERROR WITH deleteNote: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

}
