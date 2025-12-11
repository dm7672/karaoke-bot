package test.viewmodel.message.handler;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.ActionSelectHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionSelectHandlerTest {

    private IRepository<User, Long> userRepo;
    private ActionSelectHandler handler;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(User::getUserId);
        handler = new ActionSelectHandler();

        userRepo.save(new User(1L, "test"));
    }

    @Test
    void canHandle_returnsFalse_whenUserNotExists() {
        assertFalse(handler.canHandle(999L, userRepo, "action:add"));
    }

    @Test
    void canHandle_returnsFalse_whenTextNull() {
        assertFalse(handler.canHandle(1L, userRepo, null));
    }

    @Test
    void canHandle_returnsFalse_whenNotActionCommand() {
        assertFalse(handler.canHandle(1L, userRepo, "hello"));
    }

    @Test
    void canHandle_returnsTrue_forAdd() {
        assertTrue(handler.canHandle(1L, userRepo, "action:add"));
    }

    @Test
    void canHandle_returnsTrue_forDelete() {
        assertTrue(handler.canHandle(1L, userRepo, "action:delete"));
    }

    @Test
    void handle_setsPendingAdd() {
        List<BotMessage> resp = handler.handle(1L, userRepo, "action:add");

        assertEquals(1, resp.size());
        assertEquals("Вставьте ссылку на видео", resp.get(0).getText());

        assertEquals("add", userRepo.findById(1L).getPendingAction());
    }

    @Test
    void handle_setsPendingDelete() {
        List<BotMessage> resp = handler.handle(1L, userRepo, "action:delete");

        assertEquals(1, resp.size());
        assertEquals("Вставьте ссылку на видео", resp.get(0).getText());

        assertEquals("delete", userRepo.findById(1L).getPendingAction());
    }
}
