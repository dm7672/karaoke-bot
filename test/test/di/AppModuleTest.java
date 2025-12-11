package test.di;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import data.IRepository;
import di.AppModule;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import model.domain.parcer.YouTubeUrlParser;
import org.junit.jupiter.api.Test;
import services.youtube.IYouTubeService;
import viewmodel.message.handler.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppModuleTest {

    @Test
    void injector_bindsRepositoriesCorrectly() {
        Injector injector = Guice.createInjector(new AppModule());
        IRepository<User, Long> userRepo = injector.getInstance(Key.get(new com.google.inject.TypeLiteral<IRepository<User, Long>>() {}));
        IRepository<Video, String> videoRepo = injector.getInstance(Key.get(new com.google.inject.TypeLiteral<IRepository<Video, String>>() {}));

        assertNotNull(userRepo);
        assertNotNull(videoRepo);
    }

    @Test
    void injector_bindsUrlParserCorrectly() {
        Injector injector = Guice.createInjector(new AppModule());
        IUrlParser parser = injector.getInstance(IUrlParser.class);
        assertTrue(parser instanceof YouTubeUrlParser);
    }

    @Test
    void injector_bindsPlatformStringCorrectly() {
        Injector injector = Guice.createInjector(new AppModule());
        String platform = injector.getInstance(Key.get(String.class, Names.named("platform")));
        assertEquals("telegram", platform);
    }

    @Test
    void injector_providesYouTubeService() {
        Injector injector = Guice.createInjector(new AppModule());
        IYouTubeService service = injector.getInstance(IYouTubeService.class);
        assertNotNull(service);
    }

    @Test
    void injector_providesHandlersList() {
        Injector injector = Guice.createInjector(new AppModule());
        List<MessageHandler> handlers = injector.getInstance(
                Key.get(new com.google.inject.TypeLiteral<List<MessageHandler>>() {})
        );
        assertNotNull(handlers);
        assertEquals(9, handlers.size());
        assertTrue(handlers.stream().anyMatch(h -> h instanceof NewUserHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof HelpHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof VideosHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof MyVideosHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof AddVideoHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof DeleteVideoHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof ActionSelectHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof PendingActionHandler));
        assertTrue(handlers.stream().anyMatch(h -> h instanceof UnknownCommandHandler));
    }

}
