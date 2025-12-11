package test.viewmodel.message.handler;

import data.IRepository;
import data.InMemoryRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import model.domain.parcer.YouTubeUrlParser;
import services.youtube.IYouTubeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.AddVideoHandler;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddVideoHandlerTest {
    private static final User TEST_USER = new User(1L, "testPlatform");
    private static final String GOOD_URL = "https://www.youtube.com/watch?v=UQyei4cdGFY";
    private static final String BAD_URL = "bad";

    private IRepository<User, Long> userRepo;
    private IRepository<Video, String> videoRepo;
    private IUrlParser urlParser;
    private AddVideoHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        videoRepo = new InMemoryRepository<>(Video::getUrl);
        userRepo = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
        urlParser = new YouTubeUrlParser();
        IYouTubeService ytMock = mock(IYouTubeService.class);
        when(ytMock.addVideoToPlaylist(anyString())).thenReturn("mocked id");
        handler = new AddVideoHandler(videoRepo, urlParser, ytMock);
    }

    @Test
    void handle_goodYouTubeUrl_addsVideoAndReturnsSuccessMessage() {
        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Видео добавлено: " + GOOD_URL));

        List<Video> stored = videoRepo.findAll();
        assertEquals(1, stored.size());
        assertEquals(GOOD_URL, stored.get(0).getUrl());
        assertEquals(TEST_USER.getUserId(), stored.get(0).getUserAdded());
    }

    @Test
    void handle_duplicateYouTubeUrl_returnsExistsMessage() {
        handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);
        List<BotMessage> resp2 = handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);

        assertEquals(1, resp2.size());
        assertTrue(resp2.get(0).getText().startsWith("Видео уже существует:"));
        assertEquals(1, videoRepo.findAll().size());
    }

    @Test
    void handle_badUrl_returnsParserErrorMessage() {
        List<BotMessage> resp = handler.handle(TEST_USER.getUserId(), userRepo, BAD_URL);

        assertEquals(1, resp.size());
        assertEquals("Ссылка не принадлежит Ютубу: " + BAD_URL, resp.get(0).getText());
        assertTrue(videoRepo.findAll().isEmpty());
    }
}
