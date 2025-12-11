package test.viewmodel.message.handler;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.VideosHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VideosHandlerTest {
    private IRepository<Video, String> videoRepo;
    private VideosHandler handler;
    private static final User TEST_USER = new User(1L, "testPlatform");
    private IRepository<User, Long> userRepo;

    @BeforeEach
    void setUp() {
        videoRepo = new InMemoryRepository<>(Video::getVideoId);
        userRepo  = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
        handler   = new VideosHandler(videoRepo);
    }

    @Test
    void canHandle_onlyListVideosCommand() {
        assertTrue(handler.canHandle(1L, userRepo, "/Videos"));
        assertTrue(handler.canHandle(1L, userRepo, "/Videos     "));
        assertFalse(handler.canHandle(1L, userRepo, "/lists"));
        assertFalse(handler.canHandle(1L, userRepo, ""));
    }

    @Test
    void handle_noVideos_returnsEmptyNotice() {
        List<BotMessage> resp = handler.handle(42L, userRepo, "/Videos");
        assertEquals(1, resp.size());
        assertEquals("Ещё нет добавленных видео.", resp.get(0).getText());
    }

    @Test
    void handle_withVideos_filtersByUser() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(5L);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(6L);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<BotMessage> resp = handler.handle(5L, userRepo, "/Videos");
        assertEquals(2, resp.size());
        assertTrue(resp.stream().anyMatch(m -> m.getText().contains("u1")));
    }

    @Test
    void handle_multipleVideosForSameUser_listsAll() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(42L);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(42L);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<BotMessage> resp = handler.handle(42L, userRepo, "/Videos");
        assertEquals(2, resp.size());
        assertTrue(resp.stream().anyMatch(m -> m.getText().contains("u1")));
        assertTrue(resp.stream().anyMatch(m -> m.getText().contains("u2")));
    }
}
