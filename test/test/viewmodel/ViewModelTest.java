package test.viewmodel;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.YouTubeUrlParser;
import services.youtube.IYouTubeService;
import viewmodel.BotMessage;
import viewmodel.ViewModel;
import viewmodel.message.handler.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ViewModelTest {
    private static final Long TEST_USER = 42L;
    private static final String GOOD_URL = "https://www.youtube.com/watch?v=UQyei4cdGFY";
    private static final String BAD_URL = "bad";

    private IRepository<User, Long> userRepo;
    private IRepository<Video, String> videoRepo;
    private ViewModel vm;

    @BeforeEach
    void setUp() throws IOException {
        userRepo = new InMemoryRepository<>(User::getUserId);
        videoRepo = new InMemoryRepository<>(Video::getVideoId);

        IYouTubeService ytMock = mock(IYouTubeService.class);
        when(ytMock.addVideoToPlaylist(anyString())).thenReturn("mocked id");

        List<MessageHandler> handlers = List.of(
                new NewUserHandler(userRepo, "console"),
                new HelpHandler(),
                new VideosHandler(videoRepo),
                new MyVideosHandler(videoRepo),
                new AddVideoHandler(videoRepo, new YouTubeUrlParser(), ytMock),
                new UnknownCommandHandler()
        );

        vm = new ViewModel(userRepo, handlers);
    }

    @Test
    void newUserStart_registersUserAndReturnsWelcome() {
        List<BotMessage> resp = vm.processMessage(TEST_USER, "/start");

        assertTrue(userRepo.existsById(TEST_USER));
        assertEquals(1, resp.size());

        String txt = resp.get(0).getText();
        assertTrue(txt.contains("Добро пожаловать"));
        assertTrue(txt.contains("/help"));
    }

    @Test
    void helpCommand_afterRegistration_returnsHelpText() {
        vm.processMessage(TEST_USER, "/start");
        List<BotMessage> resp = vm.processMessage(TEST_USER, "/help");

        assertFalse(resp.isEmpty());
        assertTrue(resp.get(0).getText().startsWith("Как работать с ботом"));
    }

    @Test
    void addAndDuplicateVideo_behavesCorrectly() {
        vm.processMessage(TEST_USER, "/start");

        List<BotMessage> first = vm.processMessage(TEST_USER, GOOD_URL);
        assertEquals(1, first.size());
        assertTrue(first.get(0).getText().contains("Видео добавлено"));

        assertEquals(1, videoRepo.findAll().size());

        List<BotMessage> second = vm.processMessage(TEST_USER, GOOD_URL);
        assertEquals(1, second.size());
        assertTrue(second.get(0).getText().startsWith("Видео уже существует"));
        assertEquals(1, videoRepo.findAll().size());
    }

    @Test
    void unknownCommand_afterRegistration_returnsUnknown() {
        vm.processMessage(TEST_USER, "/start");
        List<BotMessage> resp = vm.processMessage(TEST_USER, "/foobar");

        assertEquals(2, resp.size());
        assertTrue(resp.get(0).getText().contains("Неизвестная команд"));
        assertTrue(resp.get(1).getText().contains("/help"));
    }

    @Test
    void badUrl_afterRegistration_returnsParserError() {
        vm.processMessage(TEST_USER, "/start");
        List<BotMessage> resp = vm.processMessage(TEST_USER, BAD_URL);

        assertEquals(1, resp.size());
        assertEquals("Ссылка не принадлежит Ютубу: " + BAD_URL, resp.get(0).getText());
        assertTrue(videoRepo.findAll().isEmpty());
    }

    @Test
    void startCommand_twice_doesNotDuplicateUser() {
        vm.processMessage(TEST_USER, "/start");
        assertEquals(1, userRepo.findAll().size());

        List<BotMessage> resp = vm.processMessage(TEST_USER, "/start");
        assertEquals(2, resp.size());
        assertEquals(1, userRepo.findAll().size());
    }

    @Test
    void multipleUsers_areHandledIndependently() {
        Long userA = 1L;
        Long userB = 2L;

        vm.processMessage(userA, "/start");
        vm.processMessage(userB, "/start");

        assertTrue(userRepo.existsById(userA));
        assertTrue(userRepo.existsById(userB));
        assertEquals(2, userRepo.findAll().size());

        vm.processMessage(userA, GOOD_URL);
        assertEquals(1, videoRepo.findAll().size());

        List<BotMessage> resp = vm.processMessage(userB, GOOD_URL);
        assertTrue(resp.get(0).getText().startsWith("Видео уже существует"));
    }

    @Test
    void multipleDifferentVideos_areAllStored() {
        vm.processMessage(TEST_USER, "/start");

        String url2 = "https://www.youtube.com/watch?v=abcd1234";
        String url3 = "https://www.youtube.com/watch?v=efgh5678";

        vm.processMessage(TEST_USER, GOOD_URL);
        vm.processMessage(TEST_USER, url2);
        vm.processMessage(TEST_USER, url3);

        assertEquals(3, videoRepo.findAll().size());
    }

    @Test
    void longInvalidUrl_returnsParserError() {
        vm.processMessage(TEST_USER, "/start");
        String longInvalid = "http://example.com/" + "a".repeat(300);

        List<BotMessage> resp = vm.processMessage(TEST_USER, longInvalid);

        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("Ссылка не принадлежит Ютубу"));
        assertTrue(videoRepo.findAll().isEmpty());
    }
}
