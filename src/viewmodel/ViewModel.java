package viewmodel;

import model.domain.entities.*;
import model.data.*;
import model.domain.parcer.IUrlParser;
import viewmodel.message.handler.*;

import java.util.List;

public class ViewModel {
    private final IRepository<User, Long> userRepo;
    private final List<MessageHandler>      handlers;

    public ViewModel(
            IRepository<User, Long> userRepo,
            IRepository<Video, String> videoRepo,
            IUrlParser urlParser,
            String platform) {
        this.userRepo  = userRepo;
        this.handlers = List.of(
                new NewUserHandler(userRepo, platform),      // 1. регистрация нового юзера
                new HelpHandler(),                           // 2. /help
                new VideosHandler(videoRepo),                // 3. /Videos
                new MyVideosHandler(videoRepo),
                new AddVideoHandler(videoRepo, urlParser),   // 4. добавление видео
                new UnknownCommandHandler()                 // 5. всё остальное — неизвестная команда
        );
    }

    public List<String> processMessage(Long userId, String text) {
        // Ищем первый подходящий обработчик
        for (MessageHandler h : handlers) {
            if (h.canHandle(userId,userRepo,text)) {
                return h.handle(userId,userRepo, text);
            }
        }
        // на случай, если ни один не сработал
        return List.of();
    }
}
