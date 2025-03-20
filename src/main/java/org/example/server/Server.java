package org.example.server;

import org.apache.log4j.Logger;
import org.example.server.manager.DBManager;
import org.example.server.manager.WikiManager;

import java.text.SimpleDateFormat;
import java.util.List;
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class);
    private static final WikiManager wikiManager = new WikiManager();
    private static final DBManager dbManager = new DBManager();

    public String echo(String s) {
        logger.info("ECHO CHECK.");
        return "PONG: " + s;
    }

    public synchronized String addNote(String topic, String text, String timestamp) {
        logger.info("ADD TOPIC: " + topic + " AT : " + timestamp + ", AND CONTENT: " + text + ".");
        return dbManager.addNote(topic, text, timestamp);
    }

    public String getNote(String topic) {
        logger.info("GET TOPIC: " + topic + ".");
        return dbManager.getNotes(topic);
    }

    public synchronized String deleteNote(String topic, String timestamp) {
        logger.info("DELETE TOPIC: " + topic + " AT: " + timestamp + ".");
        return dbManager.deleteNote(topic, timestamp);
    }

    public synchronized String getWikipediaSuggestions(String topic) {
        logger.info("GET WIKI TOPIC: " + topic + ".");
        try {
            List<String> suggestions = wikiManager.getWikipediaSuggestions(topic);

            if (suggestions == null || suggestions.isEmpty()) {
                return "No Wikipedia suggestions found for this topic.";
            }

            StringBuilder sb = new StringBuilder("Wikipedia Suggestions:\n");
            for (int i = 0; i < suggestions.size(); i++) {
                sb.append(suggestions.get(i)).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR fetching Wikipedia suggestions.";
        }
    }

    public synchronized String saveSelectedLink(String topic, String selectedLink) {
        logger.info("ADD WIKI TOPIC: " + topic + ", WITH CONTENT-LINK: " + selectedLink + ".");
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            dbManager.addNote(topic, selectedLink, timestamp);

            return "Successfully saved the selected link: " + selectedLink;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving selected link.";
        }
    }
}
