package services.youtube;

import io.github.cdimascio.dotenv.Dotenv;

public class YouTubeInitializer {

    public static IYouTubeService init() {
        try {
            Dotenv dotenv = Dotenv.load();
            String playlistId = dotenv.get("YT_PLAYLIST_ID");
            if (playlistId != null && !playlistId.isBlank()) {
                System.out.println("YouTubeService получен");
                return new YouTubeService();
            } else {
                System.out.println("YT_PLAYLIST_ID не задан - запускаем без YouTube.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не удалось инициализировать YouTubeService");
        }
        return null;
    }
}
