import model.data.*;
import model.domain.entities.*;
import model.domain.parcer.*;
import view.*;
import viewmodel.*;

public class Main {
    public static void main(String[] args) {
        IRepository<User, Long> userRepo = new InMemoryRepository<>(User::getUserId);
        IRepository<Video, String> videoRepo = new InMemoryRepository<>(Video::getVideoId);
        IUrlParser urlParser = new YouTubeUrlParser();

        //String platform = "console";
        String platform = "telegram";

        ViewModel vm = new ViewModel(userRepo, videoRepo, urlParser, platform);

        //ConsoleView view = new ConsoleView();
        TelegramView view = new TelegramView();

        view.setViewModel(vm);
        view.start();
    }
}