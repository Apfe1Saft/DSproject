package org.example.server.manager;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WikiManager {
    private static final String WIKIPEDIA_API = "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=";

    public List<String> getWikipediaSuggestions(String topic) {
        try {
            String apiUrl = WIKIPEDIA_API + topic.replace(" ", "%20");
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return formatWikipediaResponse(content.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error fetching Wikipedia data.");
        }
    }

    private List<String> formatWikipediaResponse(String jsonResponse) {
        List<String> formattedList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            JSONArray titles = jsonArray.getJSONArray(1);
            JSONArray links = jsonArray.getJSONArray(3);

            for (int i = 0; i < titles.length(); i++) {
                String title = titles.getString(i);
                String link = links.getString(i);
                formattedList.add((i + 1) + ". " + title + " - " + link);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error parsing Wikipedia response.");
        }
        return formattedList;
    }
}