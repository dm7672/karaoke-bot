
import model.data.*;
import model.domain.entities.*;
import model.domain.parcer.*;
import view.*;
import viewmodel.*;

public class Main {
    public static void main(String[] args) {
        IRepository<User, Integer> userRepo  =
                new InMemoryRepository<>(User::getUserId);

        IRepository<Video, String> videoRepo =
                new InMemoryRepository<>(Video::getVideoId);
        IUrlParser urlParser = new YouTubeUrlParser();
        String platform   = "console";
        ViewModel vm = new ViewModel(userRepo, videoRepo, urlParser, platform);

        ConsoleView view = new ConsoleView();
        view.setViewModel(vm);
        view.start();
    }
}