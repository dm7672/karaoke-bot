package viewmodel.message.handler;

import data.IRepository;
import model.domain.entities.User;

import java.util.List;

public class HelpHandler implements MessageHandler {
    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        return "/help".equalsIgnoreCase(text.trim()) && userRepo.existsById(userId);
    }

    @Override
    public List<String> handle(Long userId,IRepository<User, Long> userRepo, String text) {
        return List.of(
                "Как работать с ботом:",
                "  • Отправьте URL видео — оно будет добавлено, если его ещё нет.",
                "  • /MyVideos — показать ваши видео.",
                "  • /Videos — показать все видео.",
                "  • /help — эта помощь."
        );
    }
}
