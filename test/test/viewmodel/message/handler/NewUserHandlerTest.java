package test.viewmodel.message.handler;

import viewmodel.message.handler.*;
import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NewUserHandlerTest {
    private IRepository<User, Long> userRepo;
    private NewUserHandler handler;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(User::getUserId);
        handler  = new NewUserHandler(userRepo, "testPlat");
    }

    @Test
    void canHandle_whenUserNotExists() {
        assertTrue(handler.canHandle(99L, userRepo, "any"));
    }

    @Test
    void handle_startCommand_savesUserAndWelcomes() {
        List<BotMessage> resp = handler.handle(7L, userRepo, "/start");

        assertTrue(userRepo.existsById(7L));
        assertEquals(1, resp.size());

        String txt = resp.get(0).getText();
        assertTrue(txt.contains("Добро пожаловать"));
        assertTrue(txt.contains("/help"));
    }

    @Test
    void handle_otherMessage_savesUserAndHintsHelp() {
        List<BotMessage> resp = handler.handle(8L, userRepo, "hello");

        assertTrue(userRepo.existsById(8L));
        assertEquals(1, resp.size());

        String txt = resp.get(0).getText();
        assertTrue(txt.contains("/help"));
    }

    @Test
    void canHandle_whenUserAlreadyExists_returnsFalse() {
        handler.handle(10L, userRepo, "/start");
        assertFalse(handler.canHandle(10L, userRepo, "any"));
    }

    @Test
    void handle_startCommand_twice_doesNotDuplicateUser() {
        handler.handle(11L, userRepo, "/start");
        int countAfterFirst = userRepo.findAll().size();

        handler.handle(11L, userRepo, "/start");
        int countAfterSecond = userRepo.findAll().size();

        assertEquals(countAfterFirst, countAfterSecond);
    }

    @Test
    void handle_withEmptyMessage_savesUserAndHintsHelp() {
        List<BotMessage> resp = handler.handle(13L, userRepo, "");

        assertTrue(userRepo.existsById(13L));
        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("/help"));
    }

    @Test
    void handle_withWhitespaceMessage_savesUserAndHintsHelp() {
        List<BotMessage> resp = handler.handle(14L, userRepo, "   ");

        assertTrue(userRepo.existsById(14L));
        assertEquals(1, resp.size());
        assertTrue(resp.get(0).getText().contains("/help"));
    }
}
