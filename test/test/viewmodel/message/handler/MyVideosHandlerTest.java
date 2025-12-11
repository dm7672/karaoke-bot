package test.viewmodel.message.handler;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.MyVideosHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyVideosHandlerTest {
    private IRepository<Video, String> videoRepo;
    private MyVideosHandler handler;
    private static final User TEST_USER = new User(1L, "testPlatform");
    private IRepository<User, Long> userRepo;

    @BeforeEach
    void setUp() {
        videoRepo = new InMemoryRepository<>(Video::getVideoId);
        userRepo  = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
        handler   = new MyVideosHandler(videoRepo);
    }

    @Test
    void canHandle_onlyListVideosCommand() {
        assertTrue(handler.canHandle(1L, userRepo, "/MyVideos"));
        assertTrue(handler.canHandle(1L, userRepo, "/MyVideos     "));
        assertFalse(handler.canHandle(1L, userRepo, "/lists"));
    }

    @Test
    void handle_noVideos_returnsEmptyNotice() {
        List<BotMessage> resp = handler.handle(42L, userRepo, "/MyVideos");
        assertEquals(1, resp.size());
        assertEquals("У вас ещё нет добавленных видео.", resp.get(0).getText());
    }

    @Test
    void handle_withVideos_filtersByUser() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(5L);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(6L);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<BotMessage> resp = handler.handle(5L, userRepo, "/MyVideos");
        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("u1"));
    }
}
