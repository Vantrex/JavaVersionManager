package de.vantrex.jvm.http;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class GistFetcher {

    private static final String GIST_URL = "https://gist.githubusercontent.com/Vantrex/7f4832a19747ec65dbd81b78296402c2/raw/jdk-list.json";

    public Optional<JSONObject> fetchGist() throws IOException {
        final URL url = new URL(GIST_URL);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        final int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return Optional.of(new JSONObject(content.toString()));
            }
        }
        return Optional.empty();
    }
}