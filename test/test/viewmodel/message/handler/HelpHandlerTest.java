package test.viewmodel.message.handler;

import model.data.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import viewmodel.message.handler.HelpHandler;
import model.data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HelpHandlerTest {
    private final HelpHandler handler = new HelpHandler();
    private static final User TEST_USER = new User(1L, "testPlatform");
    private IRepository<User, Long> userRepo;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
    }

    @Test
    void canHandle_onlyHelpCommand() {
        assertTrue(handler.canHandle(1L, userRepo, "/help"));
        assertTrue(handler.canHandle(1L, userRepo, "/help    "));
        assertFalse(handler.canHandle(1L, userRepo, "/HELPX"));
        assertFalse(handler.canHandle(1L, userRepo, "help"));
    }

    @Test
    void handle_returnsHelpText() {
        List<String> resp = handler.handle(1L, userRepo, "/help");
        assertFalse(resp.isEmpty());
        assertTrue(resp.get(0).startsWith("Как работать с ботом:"));
    }

    @Test
    void canHandle_withExtraSpacesInsideFails() {
        assertFalse(handler.canHandle(1L, userRepo, "/he lp"));
        assertFalse(handler.canHandle(1L, userRepo, "/ help"));
    }

    @Test
    void handle_alwaysReturnsNonEmptyList() {
        List<String> resp1 = handler.handle(1L, userRepo, "/help");
        assertNotNull(resp1);
        assertFalse(resp1.isEmpty());

        List<String> resp2 = handler.handle(1L, userRepo, "/help   ");
        assertNotNull(resp2);
        assertFalse(resp2.isEmpty());
    }

    @Test
    void handle_trimsTrailingSpaces() {
        List<String> resp = handler.handle(1L, userRepo, "/help   ");
        assertNotNull(resp);
        assertFalse(resp.isEmpty());
        assertTrue(resp.get(0).contains("ботом"));
    }

    @Test
    void handle_multipleCallsConsistent() {
        List<String> first = handler.handle(1L, userRepo, "/help");
        List<String> second = handler.handle(1L, userRepo, "/help");
        assertEquals(first, second, "Результат должен быть одинаковым при повторных вызовах");
    }
}
