package viewmodel;

import model.domain.entities.*;
import model.data.*;
import model.domain.parcer.IUrlParser;
import viewmodel.message.handler.*;
import model.domain.youtube.*;

import java.util.List;

public class ViewModel {
    private final IRepository<User, Long> userRepo;
    private final List<MessageHandler> handlers;

    public ViewModel(
            IRepository<User, Long> userRepo,
            IRepository<Video, String> videoRepo,
            IUrlParser urlParser,
            String platform) {
        this.userRepo  = userRepo;

        // Получаем YouTubeService (может быть null) из holder
        var yt = YouTubeServiceHolder.get();

        this.handlers = List.of(
                new NewUserHandler(userRepo, platform),
                new HelpHandler(),
                new VideosHandler(videoRepo),
                new MyVideosHandler(videoRepo),
                new AddVideoHandler(videoRepo, urlParser, yt),
                new UnknownCommandHandler()
        );
    }

    public List<String> processMessage(Long userId, String text) {
        for (MessageHandler h : handlers) {
            if (h.canHandle(userId, userRepo, text)) {
                return h.handle(userId, userRepo, text);
            }
        }
        return List.of();
    }
}
