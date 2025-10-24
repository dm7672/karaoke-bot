package test.viewmodel;

import model.data.InMemoryRepository;
import model.data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.YouTubeUrlParser;
import viewmodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewModelTest {
    private static final Integer TEST_USER = 42;
    private static final String  GOOD_URL  = "https://www.youtube.com/watch?v=UQyei4cdGFY";
    private static final String  BAD_URL   = "bad";

    private IRepository<User, Integer>   userRepo;
    private IRepository<Video, String>   videoRepo;
    private ViewModel                    vm;

    @BeforeEach
    void setUp() {
        userRepo  = new InMemoryRepository<>(User::getUserId);
        videoRepo = new InMemoryRepository<>(Video::getVideoId);
        vm        = new ViewModel(
                userRepo,
                videoRepo,
                new YouTubeUrlParser(),
                "console"
        );
    }

    @Test
    void newUserStart_registersUserAndReturnsWelcome() {
        List<String> resp = vm.processMessage(TEST_USER, "/start");

        assertTrue(userRepo.existsById(TEST_USER),
                "Пользователь должен быть сохранён после /start");
        assertEquals(2, resp.size(),
                "Приветствие должно содержать две строки");
        assertTrue(resp.get(0).contains("Добро пожаловать"),
                "В первой строке должно быть приветствие пользователя");
        assertTrue(resp.get(1).contains("/help"),
                "Вторая строка должна подсказать функционал бота /help");
    }

    @Test
    void helpCommand_afterRegistration_returnsHelpText() {
        vm.processMessage(TEST_USER, "/start");
        List<String> resp = vm.processMessage(TEST_USER, "/help");

        assertFalse(resp.isEmpty(), "Текст помощи не должен быть пустым");
        assertTrue(resp.get(0).startsWith("Как работать с ботом:"),
                "Первая строка помощи должна объяснять использование бота");
    }

    @Test
    void addAndDuplicateVideo_behavesCorrectly() {
        vm.processMessage(TEST_USER, "/start");

        // первое добавление
        List<String> first = vm.processMessage(TEST_USER, GOOD_URL);
        assertEquals(1, first.size());
        assertTrue(first.get(0).contains("Видео добавлено: " + GOOD_URL));
        assertEquals(1, videoRepo.findAll().size(),
                "Должно быть сохранено одно видео");

        // повторное добавление
        List<String> second = vm.processMessage(TEST_USER, GOOD_URL);
        assertEquals(1, second.size());
        assertTrue(second.get(0).startsWith("Видео уже существует"),
                "Повторное добавление должно быть обнаружено");
        assertEquals(1, videoRepo.findAll().size(),
                "В репозитории всё ещё должно быть только одно видео");
    }

    @Test
    void unknownCommand_afterRegistration_returnsUnknown() {
        vm.processMessage(TEST_USER, "/start");
        List<String> resp = vm.processMessage(TEST_USER, "/foobar");

        assertEquals(2, resp.size(),
                "Неизвестная команда должна вернуть две подсказки");
        assertTrue(resp.get(0).contains("Неизвестная команд"),
                "Должно уведомить о неизвестной команде");
        assertTrue(resp.get(1).contains("/help"),
                "Должно предложить /help");
    }

    @Test
    void badUrl_afterRegistration_returnsParserError() {
        vm.processMessage(TEST_USER, "/start");
        List<String> resp = vm.processMessage(TEST_USER, BAD_URL);

        assertEquals(1, resp.size(),
                "Ошибка парсера должна вернуть одно сообщение");
        assertEquals("Ссылка не принадлежит Ютубу: " + BAD_URL,
                resp.get(0),
                "Текст ошибки должен приходить из YouTubeUrlParser");
        assertTrue(videoRepo.findAll().isEmpty(),
                "Для некорректной ссылки видео не должно сохраняться");
    }
    @Test
    void startCommand_twice_doesNotDuplicateUser() {
        // первый вызов /start
        vm.processMessage(TEST_USER, "/start");
        assertEquals(1, userRepo.findAll().size(),
                "После первого /start должен быть один пользователь");

        // повторный вызов /start
        List<String> resp = vm.processMessage(TEST_USER, "/start");
        assertEquals(2, resp.size(),
                "Повторный /start должен вернуть приветствие из двух строк");
        assertEquals(1, userRepo.findAll().size(),
                "Количество пользователей не должно увеличиться при повторном /start");
    }
    @Test
    void multipleUsers_areHandledIndependently() {
        Integer userA = 1;
        Integer userB = 2;

        vm.processMessage(userA, "/start");
        vm.processMessage(userB, "/start");

        assertTrue(userRepo.existsById(userA), "Пользователь A должен быть зарегистрирован");
        assertTrue(userRepo.existsById(userB), "Пользователь B должен быть зарегистрирован");
        assertEquals(2, userRepo.findAll().size(),
                "Оба пользователя должны храниться независимо");

        // добавляем видео от пользователя A
        vm.processMessage(userA, GOOD_URL);
        assertEquals(1, videoRepo.findAll().size(),
                "Видео должно быть добавлено в общий репозиторий");

        // пользователь B добавляет то же видео
        List<String> resp = vm.processMessage(userB, GOOD_URL);
        assertTrue(resp.get(0).startsWith("Видео уже существует"),
                "Для второго пользователя тоже должно сработать обнаружение дубликата");
    }

    @Test
    void multipleDifferentVideos_areAllStored() {
        vm.processMessage(TEST_USER, "/start");
        String url2 = "https://www.youtube.com/watch?v=abcd1234";
        String url3 = "https://www.youtube.com/watch?v=efgh5678";

        vm.processMessage(TEST_USER, GOOD_URL);
        vm.processMessage(TEST_USER, url2);
        vm.processMessage(TEST_USER, url3);

        assertEquals(3, videoRepo.findAll().size(),
                "Все три разных видео должны быть сохранены");
    }

    @Test
    void longInvalidUrl_returnsParserError() {
        vm.processMessage(TEST_USER, "/start");
        String longInvalid = "http://example.com/" + "a".repeat(300);
        List<String> resp = vm.processMessage(TEST_USER, longInvalid);

        assertEquals(1, resp.size(),
                "Длинная некорректная ссылка должна вернуть одно сообщение");
        assertTrue(resp.get(0).contains("Ссылка не принадлежит Ютубу"),
                "Сообщение должно явно указывать на неправильный источник");
        assertTrue(videoRepo.findAll().isEmpty(),
                "Видео не должно быть сохранено для некорректной ссылки");
    }
}
