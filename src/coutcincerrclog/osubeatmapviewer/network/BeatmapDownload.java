package coutcincerrclog.osubeatmapviewer.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class BeatmapDownload {

    public static void downloadBeatmap(int beatmapID, File file) {
        if (beatmapID <= 0)
            throw new IllegalArgumentException("beatmapID should be positive");
        String urlString = "https://osu.ppy.sh/osu/" + beatmapID;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream stream = connection.getInputStream()) {
                    Files.copy(stream, file.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (file.length() < 16) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException ignored) {}
                    throw new RuntimeException("Error downloading beatmap: file too small");
                }
            } else {
                throw new RuntimeException("Error requesting " + urlString + ": Response code " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
