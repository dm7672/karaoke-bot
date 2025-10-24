package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;

import java.util.List;

public class UnknownCommandHandler implements MessageHandler {
    @Override
    public boolean canHandle(Integer userId, IRepository<User, Integer> userRepo, String text) {
        return text.startsWith("/") && userRepo.existsById(userId);
    }

    @Override
    public List<String> handle(Integer userId,IRepository<User, Integer> userRepo, String text) {
        return List.of(
                "Неизвестная команда: " + text,
                "Для списка команд введите <userId> /help"
        );
    }
}
