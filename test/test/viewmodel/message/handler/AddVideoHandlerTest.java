package test.viewmodel.message.handler;

import model.data.IRepository;
import model.data.InMemoryRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import model.domain.parcer.YouTubeUrlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.message.handler.AddVideoHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddVideoHandlerTest {
    private static final User TEST_USER = new User(1L, "testPlatform");
    private static final String GOOD_URL = "https://www.youtube.com/watch?v=UQyei4cdGFY";
    private static final String BAD_URL = "bad";

    private IRepository<User, Long> userRepo;
    private IRepository<Video, String> videoRepo;
    private IUrlParser urlParser;
    private AddVideoHandler handler;

    @BeforeEach
    void setUp() {
        videoRepo = new InMemoryRepository<>(Video::getUrl);
        userRepo = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
        urlParser = new YouTubeUrlParser();
        handler = new AddVideoHandler(videoRepo, urlParser);
    }

    @Test
    void handle_goodYouTubeUrl_addsVideoAndReturnsSuccessMessage() {
        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).contains("Видео добавлено: " + GOOD_URL));

        List<Video> stored = videoRepo.findAll();
        assertEquals(1, stored.size());
        assertEquals(GOOD_URL, stored.get(0).getUrl());
        assertEquals(TEST_USER.getUserId(), stored.get(0).getUserAdded());
    }

    @Test
    void handle_duplicateYouTubeUrl_returnsExistsMessage() {
        handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);
        List<String> resp2 = handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);

        assertEquals(1, resp2.size());
        assertTrue(resp2.get(0).startsWith("Видео уже существует:"));
        assertEquals(1, videoRepo.findAll().size());
    }

    @Test
    void handle_badUrl_returnsParserErrorMessage() {
        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, BAD_URL);

        assertEquals(1, resp.size());
        assertEquals("Ссылка не принадлежит Ютубу: " + BAD_URL, resp.get(0));
        assertTrue(videoRepo.findAll().isEmpty());
    }

    @Test
    void handle_multipleDifferentUrls_addsAllVideos() {
        String url2 = "https://www.youtube.com/watch?v=abcd1234";
        String url3 = "https://www.youtube.com/watch?v=efgh5678";

        handler.handle(TEST_USER.getUserId(), userRepo, GOOD_URL);
        handler.handle(TEST_USER.getUserId(), userRepo, url2);
        handler.handle(TEST_USER.getUserId(), userRepo, url3);

        List<Video> stored = videoRepo.findAll();
        assertEquals(3, stored.size(),
                "Все три разных видео должны быть сохранены");
        assertTrue(stored.stream().anyMatch(v -> v.getUrl().equals(GOOD_URL)));
        assertTrue(stored.stream().anyMatch(v -> v.getUrl().equals(url2)));
        assertTrue(stored.stream().anyMatch(v -> v.getUrl().equals(url3)));
    }

    @Test
    void handle_sameUrlFromDifferentUsers_stillOneVideo() {
        Long userA = 1L;
        Long userB = 2L;

        handler.handle(userA, userRepo, GOOD_URL);
        List<String> resp = handler.handle(userB, userRepo, GOOD_URL);

        assertEquals(1, videoRepo.findAll().size(),
                "Видео должно храниться в одном экземпляре, даже если добавляют разные пользователи");
        assertTrue(resp.get(0).startsWith("Видео уже существует:"),
                "Второй пользователь должен получить сообщение о дубликате");
    }

    @Test
    void handle_emptyString_returnsParserError() {
        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "");

        assertEquals(1, resp.size(),
                "Пустая строка должна вернуть одно сообщение об ошибке");
        assertTrue(resp.get(0).toLowerCase().contains("ссылка"),
                "Сообщение должно указывать на ошибку ссылки");
        assertTrue(videoRepo.findAll().isEmpty());
    }

    @Test
    void handle_whitespaceString_returnsParserError() {
        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, "   ");

        assertEquals(1, resp.size(),
                "Строка из пробелов должна вернуть одно сообщение об ошибке");
        assertTrue(resp.get(0).toLowerCase().contains("ссылка"),
                "Сообщение должно указывать на ошибку ссылки");
        assertTrue(videoRepo.findAll().isEmpty());
    }

    @Test
    void handle_differentUsersAddingDifferentUrls_allSaved() {
        Long userA = 1L;
        Long userB = 2L;
        String url2 = "https://www.youtube.com/watch?v=xyz987";

        handler.handle(userA, userRepo, GOOD_URL);
        handler.handle(userB, userRepo, url2);

        List<Video> stored = videoRepo.findAll();
        assertEquals(2, stored.size(),
                "Два разных пользователя должны добавить два разных видео");
        assertTrue(stored.stream().anyMatch(v -> v.getUserAdded().equals(userA) && v.getUrl().equals(GOOD_URL)));
        assertTrue(stored.stream().anyMatch(v -> v.getUserAdded().equals(userB) && v.getUrl().equals(url2)));
    }

    @Test
    void handle_longInvalidUrl_returnsError() {
        String longInvalid = "http://example.com/" + "a".repeat(500);
        List<String> resp = handler.handle(TEST_USER.getUserId(), userRepo, longInvalid);

        assertEquals(1, resp.size(),
                "Длинная некорректная ссылка должна вернуть одно сообщение об ошибке");
        assertTrue(resp.get(0).contains("Ссылка не принадлежит Ютубу"),
                "Сообщение должно явно указывать на неправильный источник");
        assertTrue(videoRepo.findAll().isEmpty());
    }
}
