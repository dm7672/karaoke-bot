package view;

import com.google.inject.Inject;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import viewmodel.BotMessage;
import viewmodel.InlineButton;
import viewmodel.InlineKeyboard;
import viewmodel.ReplyKeyboard;
import viewmodel.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class TelegramView extends TelegramLongPollingBot implements View {
    private final ViewModel viewModel;
    private final Dotenv dotenv = Dotenv.load();

    @Inject
    public TelegramView(ViewModel vm) {
        this.viewModel = vm;
    }

    @Override
    public void start() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            System.out.println("Telegram бот запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Message msg = update.getMessage();
        Long userId = msg.getFrom().getId();
        String text = msg.getText();

        List<BotMessage> responses = viewModel.processMessage(userId, text);

        for (BotMessage resp : responses) {
            SendMessage reply = new SendMessage();
            reply.setChatId(msg.getChatId().toString());
            reply.setText(resp.getText());

            if (resp.getInlineKeyboard() != null) {
                reply.setReplyMarkup(toTelegramInlineKeyboard(resp.getInlineKeyboard()));
            } else {
                ReplyKeyboard rk = resp.getReplyKeyboard() != null ? resp.getReplyKeyboard() : defaultReplyKeyboard();
                reply.setReplyMarkup(toTelegramReplyKeyboard(rk));
            }

            try {
                execute(reply);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        List<BotMessage> responses = viewModel.processMessage(userId, data);

        for (BotMessage resp : responses) {
            SendMessage reply = new SendMessage();
            reply.setChatId(chatId.toString());
            reply.setText(resp.getText());

            if (resp.getInlineKeyboard() != null) {
                reply.setReplyMarkup(toTelegramInlineKeyboard(resp.getInlineKeyboard()));
            } else {
                ReplyKeyboard rk = resp.getReplyKeyboard() != null ? resp.getReplyKeyboard() : defaultReplyKeyboard();
                reply.setReplyMarkup(toTelegramReplyKeyboard(rk));
            }

            try {
                execute(reply);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private InlineKeyboardMarkup toTelegramInlineKeyboard(InlineKeyboard inlineKb) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (List<InlineButton> row : inlineKb.getRows()) {
            List<InlineKeyboardButton> tgRow = new ArrayList<>();
            for (InlineButton b : row) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(b.getText());
                btn.setCallbackData(b.getCallbackData());
                tgRow.add(btn);
            }
            rows.add(tgRow);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private ReplyKeyboardMarkup toTelegramReplyKeyboard(ReplyKeyboard replyKb) {
        List<KeyboardRow> rows = new ArrayList<>();

        for (List<String> row : replyKb.getRows()) {
            KeyboardRow tgRow = new KeyboardRow();
            tgRow.addAll(row);
            rows.add(tgRow);
        }

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);
        return markup;
    }

    private ReplyKeyboard defaultReplyKeyboard() {
        return ReplyKeyboard.simple(List.of("Команды"));
    }

    @Override
    public String getBotUsername() {
        return dotenv.get("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return dotenv.get("BOT_TOKEN");
    }
}
