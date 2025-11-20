import data.IRepository;
import data.InMemoryRepository;
import model.domain.entities.*;
import model.domain.parcer.*;
import services.youtube.YouTubeService;
import view.*;
import viewmodel.*;
import services.youtube.YouTubeInitializer;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) {
        IRepository<User, Long> userRepo = new InMemoryRepository<>(User::getUserId);
        IRepository<Video, String> videoRepo = new InMemoryRepository<>(Video::getVideoId);
        IUrlParser urlParser = new YouTubeUrlParser();
        String platform = "telegram";
        YouTubeService yt = YouTubeInitializer.init();

        ViewModel vm = new ViewModel(userRepo, videoRepo, urlParser, platform, yt);

        TelegramView view = new TelegramView(vm);
        view.start();
    }
}
