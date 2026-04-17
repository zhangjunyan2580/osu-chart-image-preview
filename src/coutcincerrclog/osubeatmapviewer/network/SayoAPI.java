package coutcincerrclog.osubeatmapviewer.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SayoAPI {

    public static int getSetIDByBeatmapID(int beatmapID) {
        if (beatmapID <= 0)
            throw new IllegalArgumentException("beatmapID should be positive");
        String urlString = "https://api.sayobot.cn/beatmapinfo?b=" + beatmapID;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream stream = connection.getInputStream()) {
                    Scanner scanner = new Scanner(stream).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    // I'm lazy since we only need sid here so we just do a simple string match
                    int index = response.indexOf("\"sid\":");
                    if (index != -1) {
                        int num = 0, i = index + 6;
                        while (i < response.length() && Character.isWhitespace(response.charAt(i)))
                            ++i;
                        if (i == response.length() || !Character.isDigit(response.charAt(i)))
                            throw new RuntimeException("Invalid response: didn't found a number after \"\\\"sid\\\":\"");
                        while (i < response.length() && Character.isDigit(response.charAt(i))) {
                            num = num * 10 + response.charAt(i) - '0';
                            ++i;
                        }
                        return num;
                    }
                    throw new RuntimeException("Invalid response: didn't found \"\\\"sid\\\":\" in response");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Error requesting " + urlString + ": Response code " + responseCode);
            }
        } catch (IOException e) {
            // This should not happen
            throw new RuntimeException(e);
        }
    }

    public static void downloadBeatmap(int setID) {

    }

}
