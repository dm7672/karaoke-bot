package test.view.TelegramView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import viewmodel.InlineKeyboard;
import viewmodel.InlineButton;
import viewmodel.ReplyKeyboard;
import view.TelegramView;
import viewmodel.BotMessage;
import viewmodel.ViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramViewTest {

    static class TestableTelegramView extends TelegramView {
        List<SendMessage> sentMessages = new ArrayList<>();
        public TestableTelegramView(ViewModel vm) {
            super(vm);
        }
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
        mockVm = mock(ViewModel.class);
        telegramView = new TestableTelegramView(mockVm);
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
                .thenReturn(List.of(
                        new BotMessage("resp1", null, null),
                        new BotMessage("resp2", null, null)
                ));

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
                .thenReturn(List.of(
                        new BotMessage("one", null, null),
                        new BotMessage("two", null, null),
                        new BotMessage("three", null, null)
                ));

        telegramView.onUpdateReceived(update);

        assertEquals(3, telegramView.sentMessages.size());
        assertEquals("one", telegramView.sentMessages.get(0).getText());
        assertEquals("three", telegramView.sentMessages.get(2).getText());
    }

    @Test
    void preservesChatId() {
        Update update = makeUpdate(999L, 777L, "chatid");

        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of(new BotMessage("msg", null, null)));

        telegramView.onUpdateReceived(update);

        assertFalse(telegramView.sentMessages.isEmpty());
        assertEquals("777", telegramView.sentMessages.get(0).getChatId());
    }

    @Test
    void responseTextMatchesViewModelOutput() {
        Update update = makeUpdate(111L, 222L, "ping");

        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of(new BotMessage("pong", null, null)));

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

    @Test
    void handlesCallbackQuery() {
        Update update = mock(Update.class);
        CallbackQuery callback = mock(CallbackQuery.class);
        Message msg = mock(Message.class);
        User user = mock(User.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callback);

        when(callback.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(123L);

        when(callback.getData()).thenReturn("callback_data");
        when(callback.getMessage()).thenReturn(msg);
        when(msg.getChatId()).thenReturn(456L);

        when(mockVm.processMessage(123L, "callback_data"))
                .thenReturn(List.of(new BotMessage("cb", null, null)));

        telegramView.onUpdateReceived(update);

        assertEquals(1, telegramView.sentMessages.size());
        assertEquals("cb", telegramView.sentMessages.get(0).getText());
    }

    @Test
    void sendsInlineKeyboard() {
        Update update = makeUpdate(1L, 2L, "hello");

        InlineKeyboard kb = new InlineKeyboard(
                List.of(
                        List.of(new InlineButton("A", "a1")),
                        List.of(new InlineButton("B", "b1"))
                )
        );

        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of(new BotMessage("text", kb, null)));

        telegramView.onUpdateReceived(update);

        SendMessage msg = telegramView.sentMessages.get(0);
        assertNotNull(msg.getReplyMarkup());
        assertTrue(msg.getReplyMarkup() instanceof InlineKeyboardMarkup);
    }

    @Test
    void sendsReplyKeyboard() {
        Update update = makeUpdate(1L, 2L, "hello");

        ReplyKeyboard rk = ReplyKeyboard.simple(List.of("One", "Two"));

        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of(new BotMessage("text", null, rk)));

        telegramView.onUpdateReceived(update);

        SendMessage msg = telegramView.sentMessages.get(0);
        assertNotNull(msg.getReplyMarkup());
        assertTrue(msg.getReplyMarkup() instanceof ReplyKeyboardMarkup);
    }

    @Test
    void ignoresMessageWithoutText() {
        Update update = mock(Update.class);
        Message msg = mock(Message.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(msg);
        when(msg.hasText()).thenReturn(false);

        telegramView.onUpdateReceived(update);

        verifyNoInteractions(mockVm);
        assertTrue(telegramView.sentMessages.isEmpty());
    }

    @Test
    void ignoresEmptyString() {
        Update update = makeUpdate(1L, 2L, "");

        telegramView.onUpdateReceived(update);

        verifyNoInteractions(mockVm);
        assertTrue(telegramView.sentMessages.isEmpty());
    }

    @Test
    void usesDefaultReplyKeyboardWhenNoneProvided() {
        Update update = makeUpdate(1L, 2L, "hello");

        when(mockVm.processMessage(anyLong(), anyString()))
                .thenReturn(List.of(new BotMessage("text", null, null)));

        telegramView.onUpdateReceived(update);

        SendMessage msg = telegramView.sentMessages.get(0);
        assertTrue(msg.getReplyMarkup() instanceof ReplyKeyboardMarkup);
    }
}
