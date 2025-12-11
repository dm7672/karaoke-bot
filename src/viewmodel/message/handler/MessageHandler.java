package viewmodel.message.handler;

import data.IRepository;
import model.domain.entities.User;
import viewmodel.BotMessage;

import java.util.List;

public interface MessageHandler {
    boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text);
    List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text);
}
