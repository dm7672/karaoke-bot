package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import viewmodel.BotMessage;
import viewmodel.InlineButton;
import viewmodel.InlineKeyboard;

import java.util.List;

public class HelpHandler implements MessageHandler {
    @Inject
    public HelpHandler() { }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        String t = text == null ? "" : text.trim();
        return (t.equalsIgnoreCase("/help") || t.equalsIgnoreCase("Команды"))
                && userRepo.existsById(userId);
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        String helpText = "Как работать с ботом";

        InlineKeyboard kb = new InlineKeyboard(
                List.of(
                        List.of(new InlineButton("Добавить видео", "action:add")),
                        List.of(new InlineButton("Удалить видео", "action:delete")),
                        List.of(new InlineButton("Мои видео", "/MyVideos")),
                        List.of(new InlineButton("Все видео", "/Videos")),
                        List.of(new InlineButton("Помощь", "/help"))
                )
        );

        BotMessage msg = new BotMessage(helpText, kb, null);
        return List.of(msg);
    }
}
