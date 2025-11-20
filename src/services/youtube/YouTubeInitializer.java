package services.youtube;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class YouTubeInitializer {

    public static YouTubeService init() {
        final Dotenv dotenv = Dotenv.load();
        try {
            String playlistId = dotenv.get("YT_PLAYLIST_ID");
            if (playlistId != null && !playlistId.isBlank()) {
                System.out.println("YouTubeService получен");
                return new YouTubeService();
            } else {
                System.out.println("YT_PLAYLIST_ID не задан - запускаем без YouTube.");
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось инициализировать YouTubeService");
        }
        return null;
    }
}