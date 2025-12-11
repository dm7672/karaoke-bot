package test.viewmodel.message.handler;

import data.InMemoryRepository;
import data.IRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import viewmodel.BotMessage;
import viewmodel.message.handler.AddVideoHandler;
import viewmodel.message.handler.DeleteVideoHandler;
import viewmodel.message.handler.PendingActionHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PendingActionHandlerTest {

    private IRepository<User, Long> userRepo;
    private AddVideoHandler addMock;
    private DeleteVideoHandler deleteMock;
    private PendingActionHandler handler;

    @BeforeEach
    void setUp() {
        userRepo = new InMemoryRepository<>(User::getUserId);

        addMock = mock(AddVideoHandler.class);
        deleteMock = mock(DeleteVideoHandler.class);

        handler = new PendingActionHandler(addMock, deleteMock);

        userRepo.save(new User(1L, "test"));
    }

    @Test
    void canHandle_returnsFalse_whenNoPendingAction() {
        assertFalse(handler.canHandle(1L, userRepo, "text"));
    }

    @Test
    void canHandle_returnsTrue_whenPendingActionExists() {
        User u = userRepo.findById(1L);
        u.setPendingAction("add");
        userRepo.update(u);

        assertTrue(handler.canHandle(1L, userRepo, "text"));
    }

    @Test
    void handle_callsAddHandler_whenPendingAdd() {
        User u = userRepo.findById(1L);
        u.setPendingAction("add");
        userRepo.update(u);

        when(addMock.handle(1L, userRepo, "url"))
                .thenReturn(List.of(BotMessage.textOnly("added")));

        List<BotMessage> resp = handler.handle(1L, userRepo, "url");

        assertEquals(1, resp.size());
        assertEquals("added", resp.get(0).getText());
        verify(addMock, times(1)).handle(1L, userRepo, "url");

        assertNull(userRepo.findById(1L).getPendingAction());
    }

    @Test
    void handle_callsDeleteHandler_whenPendingDelete() {
        User u = userRepo.findById(1L);
        u.setPendingAction("delete");
        userRepo.update(u);

        when(deleteMock.handle(1L, userRepo, "url"))
                .thenReturn(List.of(BotMessage.textOnly("deleted")));

        List<BotMessage> resp = handler.handle(1L, userRepo, "url");

        assertEquals(1, resp.size());
        assertEquals("deleted", resp.get(0).getText());
        verify(deleteMock, times(1)).handle(1L, userRepo, "url");

        assertNull(userRepo.findById(1L).getPendingAction());
    }

    @Test
    void handle_unknownAction_returnsError() {
        User u = userRepo.findById(1L);
        u.setPendingAction("weird");
        userRepo.update(u);

        List<BotMessage> resp = handler.handle(1L, userRepo, "something");

        assertEquals(1, resp.size());
        assertEquals("Неизвестное действие", resp.get(0).getText());
    }
}
