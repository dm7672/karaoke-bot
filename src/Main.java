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
        Injector injector = Guice.createInjector(new AppModule());
        TelegramView view = injector.getInstance(TelegramView.class);
        view.start();
    }
}

