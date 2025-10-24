package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;

import java.util.List;

public class HelpHandler implements MessageHandler {
    @Override
    public boolean canHandle(Integer userId, IRepository<User, Integer> userRepo, String text) {
        return "/help".equalsIgnoreCase(text.trim()) && userRepo.existsById(userId);
    }

    @Override
    public List<String> handle(Integer userId,IRepository<User, Integer> userRepo, String text) {
        return List.of(
                "Как работать с ботом:",
                "  • Отправьте URL видео — оно будет добавлено, если его ещё нет.",
                "  • /listMyVideos — показать ваши видео.",
                "  • /listVideos — показать все видео.",
                "  • /help — эта помощь."
        );
    }
}
