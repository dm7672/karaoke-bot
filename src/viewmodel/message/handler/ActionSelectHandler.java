package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import viewmodel.BotMessage;

import java.util.List;

public class ActionSelectHandler implements MessageHandler {

    @Inject
    public ActionSelectHandler() {}

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        if (text == null || !userRepo.existsById(userId)) return false;
        return text.equals("action:add") || text.equals("action:delete");
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        User u = userRepo.findById(userId);

        if (text.equals("action:add")) {
            u.setPendingAction("add");
        } else if (text.equals("action:delete")) {
            u.setPendingAction("delete");
        }

        userRepo.update(u);

        return List.of(BotMessage.textOnly("Вставьте ссылку на видео"));
    }
}
