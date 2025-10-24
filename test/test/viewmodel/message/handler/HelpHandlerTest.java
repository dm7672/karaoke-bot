package test.viewmodel.message.handler;

import model.data.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import viewmodel.message.handler.*;
import model.data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HelpHandlerTest {
    private final HelpHandler handler = new HelpHandler();
    private static final User TEST_USER = new User(1, "testPlatform");
    private IRepository<User, Integer>   userRepo;

    @BeforeEach
    void setUp(){
        userRepo  = new InMemoryRepository<>(User::getUserId);
        userRepo  = new InMemoryRepository<>(User::getUserId);
        userRepo.save(TEST_USER);
    }
    @Test
    void canHandle_onlyHelpCommand() {
        assertTrue(handler.canHandle(1, userRepo, "/help"));
        assertTrue(handler.canHandle(1, userRepo, "/help    "));
        assertFalse(handler.canHandle(1, userRepo, "/HELPX"));
        assertFalse(handler.canHandle(1, userRepo, "help"));
    }

    @Test
    void handle_returnsHelpText() {
        List<String> resp = handler.handle(1, userRepo, "/help");
        assertFalse(resp.isEmpty());
        assertTrue(resp.get(0).startsWith("Как работать с ботом:"));
    }
}
