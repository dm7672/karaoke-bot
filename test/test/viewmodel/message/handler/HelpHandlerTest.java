package test.viewmodel.message.handler;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.HelpHandler;

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
        List<BotMessage> resp = handler.handle(1L, userRepo, "/help");
        assertFalse(resp.isEmpty());
        assertTrue(resp.get(0).getText().startsWith("Как работать с ботом"));
    }
}
