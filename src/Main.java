import io.github.cdimascio.dotenv.Dotenv;
import model.data.*;
import model.domain.entities.*;
import model.domain.parcer.*;
import view.*;
import viewmodel.*;
import model.domain.youtube.YouTubeInitializer;

public class Main {
    public static void main(String[] args) {
        IRepository<User, Long> userRepo = new InMemoryRepository<>(User::getUserId);
        IRepository<Video, String> videoRepo = new InMemoryRepository<>(Video::getVideoId);
        IUrlParser urlParser = new YouTubeUrlParser();
        String platform = "telegram";

        YouTubeInitializer.init();

        ViewModel vm = new ViewModel(userRepo, videoRepo, urlParser, platform);

        TelegramView view = new TelegramView();
        view.setViewModel(vm);
        view.start();
    }
}
