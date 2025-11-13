package view;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import io.github.cdimascio.dotenv.Dotenv;
import viewmodel.ViewModel;

import java.util.List;

public class TelegramView extends TelegramLongPollingBot implements View {
    private ViewModel viewModel;
    private final Dotenv dotenv = Dotenv.load();

    @Override
    public void setViewModel(ViewModel vm) {
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
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Message msg = update.getMessage();
        Long userId = msg.getFrom().getId();
        String text = msg.getText();

        List<String> responses = viewModel.processMessage(userId, text);

        for (String resp : responses) {
            SendMessage reply = new SendMessage();
            reply.setChatId(msg.getChatId().toString());
            reply.setText(resp);
            try {
                execute(reply);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
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
