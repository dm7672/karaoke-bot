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
        List<String> resp = handler.handle(42L, userRepo, "/MyVideos");
        assertEquals(1, resp.size());
        assertEquals("У вас ещё нет добавленных видео.", resp.get(0));
    }

    @Test
    void handle_withVideos_filtersByUser() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(5L);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(6L);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<String> resp = handler.handle(5L, userRepo, "/MyVideos");
        assertEquals(1, resp.size());
        assertTrue(resp.get(0).contains("u1"));
    }

    @Test
    void handle_multipleVideosForSameUser_listsAll() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(42L);
        Video v2 = new Video("u2","p","id2",0,"t");
        v2.setUserAdded(42L);
        videoRepo.save(v1);
        videoRepo.save(v2);

        List<String> resp = handler.handle(42L, userRepo, "/MyVideos");
        assertEquals(2, resp.size(),
                "Оба видео одного пользователя должны быть выведены");
        assertTrue(resp.stream().anyMatch(s -> s.contains("u1")));
        assertTrue(resp.stream().anyMatch(s -> s.contains("u2")));
    }

    @Test
    void handle_userWithNoVideos_returnsEmptyNotice() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(99L);
        videoRepo.save(v1);

        List<String> resp = handler.handle(42L, userRepo, "/MyVideos");
        assertEquals(1, resp.size(),
                "Если у пользователя нет видео, должен быть один ответ");
        assertEquals("У вас ещё нет добавленных видео.", resp.get(0));
    }

    @Test
    void handle_withNullMessage_returnsEmptyNotice() {
        List<String> resp = handler.handle(42L, userRepo, null);
        assertEquals(1, resp.size(),
                "При null-сообщении должен вернуться один ответ");
        assertEquals("У вас ещё нет добавленных видео.", resp.get(0));
    }

    @Test
    void handle_otherUsersVideos_notIncluded() {
        Video v1 = new Video("u1","p","id1",0,"t");
        v1.setUserAdded(100L);
        videoRepo.save(v1);

        List<String> resp = handler.handle(200L, userRepo, "/MyVideos");
        assertEquals(1, resp.size(),
                "Если у пользователя нет своих видео, должен быть один ответ");
        assertEquals("У вас ещё нет добавленных видео.", resp.get(0));
    }

    @Test
    void handle_withWhitespaceMessage_returnsEmptyNotice() {
        List<String> resp = handler.handle(42L, userRepo, "   ");
        assertEquals(1, resp.size(),
                "При сообщении из пробелов должен вернуться один ответ");
        assertEquals("У вас ещё нет добавленных видео.", resp.get(0));
    }
}
