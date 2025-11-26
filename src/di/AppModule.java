package di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import data.IRepository;
import model.domain.entities.*;
import model.domain.parcer.IUrlParser;
import model.domain.parcer.YouTubeUrlParser;
import services.youtube.IYouTubeService;
import services.youtube.YouTubeService;
import viewmodel.message.handler.*;
import data.sqlite.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        DatabaseInitializer.init();

        bind(new com.google.inject.TypeLiteral<IRepository<User, Long>>() {})
                .toInstance(new SQLiteUserRepository());
        bind(new com.google.inject.TypeLiteral<IRepository<Video, String>>() {})
                .toInstance(new SQLiteVideoRepository());

        bind(IUrlParser.class).to(YouTubeUrlParser.class).in(Singleton.class);

        bind(String.class).annotatedWith(Names.named("platform")).toInstance("telegram");

        bind(NewUserHandler.class).in(Singleton.class);
        bind(HelpHandler.class).in(Singleton.class);
        bind(VideosHandler.class).in(Singleton.class);
        bind(MyVideosHandler.class).in(Singleton.class);
        bind(AddVideoHandler.class).in(Singleton.class);
        bind(UnknownCommandHandler.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public IYouTubeService provideYouTubeService() {
        try {
            return new YouTubeService();
        } catch (GeneralSecurityException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Provides
    @Singleton
    public List<MessageHandler> provideHandlers(
            NewUserHandler newUser,
            HelpHandler help,
            VideosHandler videos,
            MyVideosHandler myVideos,
            AddVideoHandler addVideo,
            UnknownCommandHandler unknown
    ) {
        return List.of(
                newUser,
                help,
                videos,
                myVideos,
                addVideo,
                unknown
        );
    }
}
