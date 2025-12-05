package test.viewmodel.message.handler;

import data.IRepository;
import data.InMemoryRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import model.domain.parcer.YouTubeUrlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.youtube.IYouTubeService;
import viewmodel.message.handler.DeleteVideoHandler;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteVideoHandlerTest {
    private static final User TEST_USER = new User(1L, "testPlatform");
    private static final User OTHER_USER = new User(2L, "testPlatform");
    private static final String VIDEO_ID = "UQyei4cdGFY";
    private static final String VIDEO_URL = "https://www.youtube.com/watch?v=" + VIDEO_ID;

    private IRepository<User, Long> userRepo;
    private IRepository<Video, String> videoRepo;
    private IUrlParser urlParser;
    private IYouTubeService ytMock;
    private DeleteVideoHandler handler;

    @BeforeEach
    void setUp() {
        videoRepo = new InMemoryRepository<>(Video::getVideoId);
        userRepo = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
        userRepo.save(OTHER_USER);
        urlParser = new YouTubeUrlParser();
        ytMock = mock(IYouTubeService.class);
        handler = new DeleteVideoHandler(videoRepo, urlParser, ytMock);
    }

    @Test
    void handle_deleteById_callsYouTubeAndDeletesFromDb_whenYouTubeSucceeds() throws Exception {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        v.setPlaylistItemId("playlistItem123");
        videoRepo.save(v);

        doNothing().when(ytMock).removeVideoFromPlaylist("playlistItem123");

        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).contains("Видео удалено: " + VIDEO_URL));
        assertTrue(videoRepo.findAll().isEmpty());
        verify(ytMock, times(1)).removeVideoFromPlaylist("playlistItem123");
    }

    @Test
    void handle_deleteByUrl_callsYouTubeAndDeletesFromDb_whenYouTubeSucceeds() throws Exception {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        v.setPlaylistItemId("plItemX");
        videoRepo.save(v);

        doNothing().when(ytMock).removeVideoFromPlaylist("plItemX");

        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_URL);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).contains("Видео удалено: " + VIDEO_URL));
        assertTrue(videoRepo.findAll().isEmpty());
        verify(ytMock, times(1)).removeVideoFromPlaylist("plItemX");
    }

    @Test
    void handle_deleteWithoutPlaylistItem_deletesOnlyFromDb_andDoesNotCallYouTube() {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        videoRepo.save(v);

        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).contains("Видео удалено: " + VIDEO_URL));
        assertTrue(videoRepo.findAll().isEmpty());
        verifyNoInteractions(ytMock);
    }

    @Test
    void handle_youtubeDeletionFails_reportsErrorAndStillDeletesFromDb() throws Exception {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        v.setPlaylistItemId("pl-fail");
        videoRepo.save(v);

        doThrow(new IOException("yt error")).when(ytMock).removeVideoFromPlaylist("pl-fail");

        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(2, resp.size());
        assertTrue(resp.stream().anyMatch(s -> s.contains("Не удалось удалить из YouTube-плейлиста")));
        assertTrue(resp.stream().anyMatch(s -> s.contains("Видео удалено: " + VIDEO_URL)));
        assertTrue(videoRepo.findAll().isEmpty());
        verify(ytMock, times(1)).removeVideoFromPlaylist("pl-fail");
    }

    @Test
    void handle_cannotDeleteOthersVideo_returnsForbiddenAndKeepsRecord() {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(OTHER_USER.getUserId());
        v.setPlaylistItemId("pl-other");
        videoRepo.save(v);

        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(1, resp.size());
        assertEquals("Вы не можете удалить чужое видео", resp.get(0));
        assertEquals(1, videoRepo.findAll().size());
        verifyNoInteractions(ytMock);
    }

    @Test
    void handle_nonexistentVideo_returnsNotFound() {
        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete no-such-id");

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).contains("Видео не найдено"));
    }

    @Test
    void handle_noArgument_returnsUsageMessage() {
        List<String> resp1 = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete");
        List<String> resp2 = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete   ");

        assertEquals(1, resp1.size());
        assertTrue(resp1.get(0).toLowerCase().contains("использование"));
        assertEquals(1, resp2.size());
        assertTrue(resp2.get(0).toLowerCase().contains("использование"));
    }
}
