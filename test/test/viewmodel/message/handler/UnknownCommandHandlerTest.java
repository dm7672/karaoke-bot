package test.viewmodel.message.handler;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.UnknownCommandHandler;

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
        List<BotMessage> resp = handler.handle(1L, userRepo, "/foo");
        assertEquals(2, resp.size());
        assertTrue(resp.get(0).getText().contains("Неизвестная команда"));
        assertTrue(resp.get(1).getText().contains("/help"));
    }
}
