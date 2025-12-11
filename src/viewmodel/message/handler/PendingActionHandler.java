package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import viewmodel.BotMessage;

import java.util.List;

public class PendingActionHandler implements MessageHandler {

    private final AddVideoHandler addHandler;
    private final DeleteVideoHandler deleteHandler;

    @Inject
    public PendingActionHandler(AddVideoHandler addHandler, DeleteVideoHandler deleteHandler) {
        this.addHandler = addHandler;
        this.deleteHandler = deleteHandler;
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        if (text == null || !userRepo.existsById(userId)) return false;
        User u = userRepo.findById(userId);
        return u.getPendingAction() != null;
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        User u = userRepo.findById(userId);
        String action = u.getPendingAction();
        u.setPendingAction(null);
        userRepo.update(u);

        if ("add".equals(action)) {
            return addHandler.handle(userId, userRepo, text);
        }

        if ("delete".equals(action)) {
            return deleteHandler.handle(userId, userRepo, text);
        }

        return List.of(BotMessage.textOnly("Неизвестное действие"));
    }
}
