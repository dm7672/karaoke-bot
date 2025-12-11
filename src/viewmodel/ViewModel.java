package viewmodel;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import viewmodel.message.handler.MessageHandler;

import java.util.List;

public class ViewModel {
    private final IRepository<User, Long> userRepo;
    private final List<MessageHandler> handlers;

    @Inject
    public ViewModel(IRepository<User, Long> userRepo,
                     List<MessageHandler> handlers) {
        this.userRepo = userRepo;
        this.handlers = handlers;
    }

    public List<BotMessage> processMessage(Long userId, String text) {
        for (MessageHandler h : handlers) {
            if (h.canHandle(userId, userRepo, text)) {
                return h.handle(userId, userRepo, text);
            }
        }
        return List.of(BotMessage.textOnly("Команда не обработана"));
    }
}
