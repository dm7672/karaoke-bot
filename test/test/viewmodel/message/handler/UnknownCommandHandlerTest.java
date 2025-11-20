package test.viewmodel.message.handler;

import data.IRepository;
import data.InMemoryRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import viewmodel.message.handler.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnknownCommandHandlerTest {
    private final UnknownCommandHandler handler = new UnknownCommandHandler();
    private static final User TEST_USER = new User(1L, "testPlatform");
    private IRepository<User, Long> userRepo;

    @BeforeEach
    void setUp(){
        userRepo  = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
    }

    @Test
    void canHandle_onlySlashCommands() {
        assertTrue(handler.canHandle(1L, userRepo, "/x"));
        assertFalse(handler.canHandle(1L, userRepo, "x"));
    }

    @Test
    void handle_returnsUnknownNotice() {
        List<String> resp = handler.handle(1L, null, "/foo");
        assertEquals(2, resp.size());
        assertTrue(resp.get(0).contains("Неизвестная команда"));
        assertTrue(resp.get(1).contains("/help"));
    }

    @Test
    void handle_withEmptyMessage_returnsUnknownNotice() {
        List<String> resp = handler.handle(1L, null, "");
        assertEquals(2, resp.size(),
                "Даже при пустом сообщении должно вернуться два ответа");
        assertTrue(resp.get(0).contains("Неизвестная команда"));
        assertTrue(resp.get(1).contains("/help"));
    }

    @Test
    void handle_withNullMessage_returnsUnknownNotice() {
        List<String> resp = handler.handle(1L, null, null);
        assertEquals(2, resp.size(),
                "Даже при null-сообщении должно вернуться два ответа");
        assertTrue(resp.get(0).contains("Неизвестная команда"));
        assertTrue(resp.get(1).contains("/help"));
    }

    @Test
    void handle_alwaysContainsHelpHint() {
        List<String> resp = handler.handle(1L, null, "/something");
        assertTrue(resp.stream().anyMatch(s -> s.contains("/help")),
                "Ответ всегда должен содержать подсказку про /help");
    }

    @Test
    void handle_longUnknownCommand_returnsTwoLines() {
        String longCmd = "/" + "a".repeat(200);
        List<String> resp = handler.handle(1L, null, longCmd);
        assertEquals(2, resp.size(),
                "Даже для очень длинной неизвестной команды должно вернуться два ответа");
        assertTrue(resp.get(0).contains("Неизвестная команда"));
        assertTrue(resp.get(1).contains("/help"));
    }

    @Test
    void handle_alwaysTwoResponses() {
        List<String> resp1 = handler.handle(1L, null, "/abc");
        List<String> resp2 = handler.handle(1L, null, "/def");
        assertEquals(2, resp1.size(),
                "Ответ всегда должен состоять из двух строк");
        assertEquals(2, resp2.size(),
                "Ответ всегда должен состоять из двух строк");
    }
}
