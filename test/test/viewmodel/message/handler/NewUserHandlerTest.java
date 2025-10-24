package test.viewmodel.message.handler;

import viewmodel.message.handler.*;
import model.data.InMemoryRepository;
import model.data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NewUserHandlerTest {
    private IRepository<User, Integer> userRepo;
    private NewUserHandler             handler;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(User::getUserId);
        handler  = new NewUserHandler(userRepo, "testPlat");
    }

    @Test
    void canHandle_whenUserNotExists() {
        assertTrue(handler.canHandle(99, userRepo, "any"));
    }

    @Test
    void handle_startCommand_savesUserAndWelcomes() {
        List<String> resp = handler.handle(7, userRepo, "/start");

        assertTrue(userRepo.existsById(7));
        assertEquals(2, resp.size());
        assertTrue(resp.get(0).contains("Добро пожаловать"));
    }

    @Test
    void handle_otherMessage_savesUserAndHintsHelp() {
        List<String> resp = handler.handle(8, userRepo, "hello");

        assertTrue(userRepo.existsById(8));
        assertEquals(2, resp.size());
        assertTrue(resp.get(1).contains("/help"));
    }

    @Test
    void canHandle_whenUserAlreadyExists_returnsFalse() {
        // сначала создаём пользователя
        handler.handle(10, userRepo, "/start");

        // теперь canHandle должен вернуть false
        assertFalse(handler.canHandle(10, userRepo, "any"),
                "Для уже существующего пользователя обработчик не должен срабатывать");
    }

    @Test
    void handle_startCommand_twice_doesNotDuplicateUser() {
        handler.handle(11, userRepo, "/start");
        int countAfterFirst = userRepo.findAll().size();

        handler.handle(11, userRepo, "/start");
        int countAfterSecond = userRepo.findAll().size();

        assertEquals(countAfterFirst, countAfterSecond,
                "Повторный вызов /start не должен создавать дубликатов пользователей");
    }

    @Test
    void handle_withEmptyMessage_savesUserAndHintsHelp() {
        List<String> resp = handler.handle(13, userRepo, "");

        assertTrue(userRepo.existsById(13),
                "Даже при пустом сообщении пользователь должен быть сохранён");
        assertEquals(2, resp.size(),
                "Ответ должен состоять из двух строк");
        assertTrue(resp.get(1).contains("/help"),
                "Вторая строка должна содержать подсказку про /help");
    }

    @Test
    void handle_withWhitespaceMessage_savesUserAndHintsHelp() {
        List<String> resp = handler.handle(14, userRepo, "   ");

        assertTrue(userRepo.existsById(14),
                "Даже при сообщении из пробелов пользователь должен быть сохранён");
        assertEquals(2, resp.size(),
                "Ответ должен состоять из двух строк");
        assertTrue(resp.get(1).contains("/help"),
                "Вторая строка должна содержать подсказку про /help");
    }
}
