package viewmodel;

import data.IRepository;
import model.domain.entities.*;
import model.domain.parcer.IUrlParser;
import services.youtube.*;
import viewmodel.message.handler.*;

import java.util.List;

public class ViewModel {
    private final IRepository<User, Long> userRepo;
    private final List<MessageHandler> handlers;

    public ViewModel(
            IRepository<User, Long> userRepo,
            IRepository<Video, String> videoRepo,
            IUrlParser urlParser,
            String platform,
            IYouTubeService yt) {
        this.userRepo  = userRepo;

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
