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
import viewmodel.BotMessage;
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

        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Видео удалено: " + VIDEO_URL));
        assertTrue(videoRepo.findAll().isEmpty());
        verify(ytMock, times(1)).removeVideoFromPlaylist("playlistItem123");
    }

    @Test
    void handle_deleteById_noPlaylistItemId() {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        v.setPlaylistItemId(null);
        videoRepo.save(v);

        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Видео удалено"));
        assertTrue(videoRepo.findAll().isEmpty());
        verifyNoInteractions(ytMock);
    }

    @Test
    void handle_deleteWithDeletePrefix() {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        videoRepo.save(v);

        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "delete:" + VIDEO_ID);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Видео удалено"));
        assertTrue(videoRepo.findAll().isEmpty());
    }

    @Test
    void handle_invalidFormat_returnsUsage() {
        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete");

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Использование"));
    }

    @Test
    void handle_videoNotFound_returnsError() {
        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete noSuchId");

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Видео не найдено"));
    }

    @Test
    void handle_deleteOthersVideo_forbidden() {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(OTHER_USER.getUserId());
        videoRepo.save(v);

        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(1, resp.size());
        assertEquals("Вы не можете удалить чужое видео", resp.get(0).getText());
        assertEquals(1, videoRepo.findAll().size());
        verifyNoInteractions(ytMock);
    }

    @Test
    void handle_youtubeError_reportsAndStillDeletes() throws Exception {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        v.setPlaylistItemId("err123");
        videoRepo.save(v);

        doThrow(new IOException("yt fail")).when(ytMock).removeVideoFromPlaylist("err123");

        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, "/Delete " + VIDEO_ID);

        assertEquals(2, resp.size());
        assertTrue(resp.get(0).getText().contains("Не удалось удалить"));
        assertTrue(resp.get(1).getText().contains("Видео удалено"));
        assertTrue(videoRepo.findAll().isEmpty());
    }

    @Test
    void handle_deleteByValidUrlStringWithoutCommand() {
        Video v = new Video(VIDEO_URL, "YouTube", VIDEO_ID, null, "watch");
        v.setUserAdded(TEST_USER.getUserId());
        videoRepo.save(v);

        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, VIDEO_URL);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Видео удалено"));
        assertTrue(videoRepo.findAll().isEmpty());
    }

}
