import io.github.cdimascio.dotenv.Dotenv;
import model.data.*;
import model.domain.entities.*;
import model.domain.parcer.*;
import view.*;
import viewmodel.*;
import model.domain.youtube.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) {
        IRepository<User, Long> userRepo = new InMemoryRepository<>(User::getUserId);
        IRepository<Video, String> videoRepo = new InMemoryRepository<>(Video::getVideoId);
        IUrlParser urlParser = new YouTubeUrlParser();
        String platform = "telegram";
        final Dotenv dotenv = Dotenv.load();
        // Пытаемся инициализировать YouTubeService
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

        ViewModel vm = new ViewModel(userRepo, videoRepo, urlParser, platform);

        TelegramView view = new TelegramView();
        view.setViewModel(vm);
        view.start();
    }
}
