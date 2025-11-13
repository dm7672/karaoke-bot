package test.TelegramView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import view.TelegramView;
import viewmodel.ViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramViewTest {

    static class TestableTelegramView extends TelegramView {
        List<SendMessage> sentMessages = new ArrayList<>();
        @Override
        public <T extends Serializable, Method extends org.telegram.telegrambots.meta.api.methods.BotApiMethod<T>> T execute(Method method) {
            if (method instanceof SendMessage) {
                sentMessages.add((SendMessage) method);
            }
            return null;
        }
    }

    private TestableTelegramView telegramView;
    private ViewModel mockVm;

    @BeforeEach
    void setUp() {
        telegramView = new TestableTelegramView();
        mockVm = mock(ViewModel.class);
        telegramView.setViewModel(mockVm);
    }

    private Update makeUpdate(Long userId, Long chatId, String text) {
        Update update = mock(Update.class);
        Message msg = mock(Message.class);
        User user = mock(User.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(msg);
        when(msg.hasText()).thenReturn(text != null && !text.isEmpty());
        when(msg.getText()).thenReturn(text);
        when(msg.getChatId()).thenReturn(chatId);
        when(msg.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        return update;
    }

    @Test
    void sendsTwoResponses() {
        Update update = makeUpdate(123L, 456L, "hello");
        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of("resp1", "resp2"));
        telegramView.onUpdateReceived(update);
        assertEquals(2, telegramView.sentMessages.size());
        assertEquals("resp1", telegramView.sentMessages.get(0).getText());
        assertEquals("resp2", telegramView.sentMessages.get(1).getText());
    }

    @Test
    void doesNothingWithoutText() {
        Update update = makeUpdate(123L, 456L, null);
        telegramView.onUpdateReceived(update);
        verifyNoInteractions(mockVm);
        assertTrue(telegramView.sentMessages.isEmpty());
    }

    @Test
    void emptyResponseList() {
        Update update = makeUpdate(123L, 456L, "test");
        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of());
        telegramView.onUpdateReceived(update);
        assertTrue(telegramView.sentMessages.isEmpty());
    }

    @Test
    void multipleResponses() {
        Update update = makeUpdate(123L, 456L, "multi");
        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of("one", "two", "three"));
        telegramView.onUpdateReceived(update);
        assertEquals(3, telegramView.sentMessages.size());
        assertEquals("one", telegramView.sentMessages.get(0).getText());
        assertEquals("three", telegramView.sentMessages.get(2).getText());
    }

    @Test
    void preservesChatId() {
        Update update = makeUpdate(999L, 777L, "chatid");
        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of("msg"));
        telegramView.onUpdateReceived(update);
        assertFalse(telegramView.sentMessages.isEmpty());
        assertEquals("777", telegramView.sentMessages.get(0).getChatId());
    }

    @Test
    void responseTextMatchesViewModelOutput() {
        Update update = makeUpdate(111L, 222L, "ping");
        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of("pong"));
        telegramView.onUpdateReceived(update);
        assertEquals("pong", telegramView.sentMessages.get(0).getText());
    }

    @Test
    void noMessageInUpdate_doesNothing() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);
        telegramView.onUpdateReceived(update);
        verifyNoInteractions(mockVm);
        assertTrue(telegramView.sentMessages.isEmpty());
    }

    @Test
    void botUsernameNotEmpty() {
        assertNotNull(telegramView.getBotUsername());
        assertFalse(telegramView.getBotUsername().isEmpty());
    }

    @Test
    void botTokenNotEmpty() {
        assertNotNull(telegramView.getBotToken());
        assertFalse(telegramView.getBotToken().isEmpty());
    }
}
