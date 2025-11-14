package model.domain.youtube;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class YouTubeInitializer {

    public static void init() {
        final Dotenv dotenv = Dotenv.load();
        try {
            String playlistId = dotenv.get("YT_PLAYLIST_ID");
            if (playlistId != null && !playlistId.isBlank()) {
                YouTubeService yt = new YouTubeService(playlistId);
                YouTubeServiceHolder.set(yt);
                System.out.println("YouTubeService установлен в holder.");
            } else {
                System.out.println("YT_PLAYLIST_ID не задан - запускаем без YouTube.");
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось инициализировать YouTubeService - запускаем без интеграции.");
        }
    }
}