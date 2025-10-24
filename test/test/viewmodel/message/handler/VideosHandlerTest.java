package test.viewmodel.message.handler;

import model.domain.entities.User;
import viewmodel.message.handler.*;
import model.data.InMemoryRepository;
import model.data.IRepository;
import model.domain.entities.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideosHandlerTest {
    private IRepository<Video, String> videoRepo;
    private VideosHandler         handler;
    private static final User TEST_USER = new User(1, "testPlatform");
    private IRepository<User, Integer>   userRepo;

    @BeforeEach
    void setUp() {
        videoRepo = new InMemoryRepository<>(Video::getVideoId);
        userRepo  = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
        handler   = new VideosHandler(videoRepo);

    }

    @Test
    void canHandle_onlyListVideosCommand() {
        assertTrue(handler.canHandle(1, userRepo, "/Videos"));
        assertTrue(handler.canHandle(1, userRepo, "/Videos     "));
        assertFalse(handler.canHandle(1, userRepo, "/lists"));
        assertFalse(handler.canHandle(1, userRepo, ""));
    }

    @Test
    void handle_noVideos_returnsEmptyNotice() {
        List<String> resp = handler.handle(42, userRepo, "/Videos");
        assertEquals(1, resp.size());
        assertEquals("Ещё нет добавленных видео.", resp.get(0));
    }

    @Test
    void handle_withVideos_filtersByUser() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(5);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(6);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<String> resp = handler.handle(5, userRepo, "/Videos");
        assertEquals(2, resp.size());
        assertTrue(resp.contains("u1"));
    }
    @Test
    void handle_multipleVideosForSameUser_listsAll() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(42);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(42);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<String> resp = handler.handle(42, userRepo, "/Videos");
        assertEquals(2, resp.size(),
                "Оба видео одного пользователя должны быть выведены");
        assertTrue(resp.stream().anyMatch(s -> s.contains("u1")));
        assertTrue(resp.stream().anyMatch(s -> s.contains("u2")));
    }

}
