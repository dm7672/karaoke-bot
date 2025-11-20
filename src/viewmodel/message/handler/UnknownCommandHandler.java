package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;

import java.util.List;

public class UnknownCommandHandler implements MessageHandler {
    @Inject
    public UnknownCommandHandler() { }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        return text != null && text.startsWith("/") && userRepo.existsById(userId);
    }

    @Override
    public List<String> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        return List.of(
                "Неизвестная команда: " + text,
                "Для списка команд введите /help"
        );
    }
}
