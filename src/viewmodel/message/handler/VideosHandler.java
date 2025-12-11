package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import viewmodel.BotMessage;
import viewmodel.InlineButton;
import viewmodel.InlineKeyboard;

import java.util.List;
import java.util.stream.Collectors;

public class VideosHandler implements MessageHandler {
    private final IRepository<Video, String> videoRepo;

    @Inject
    public VideosHandler(IRepository<Video, String> videoRepo) {
        this.videoRepo = videoRepo;
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        return "/Videos".equalsIgnoreCase(text.trim());
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        List<Video> videos = videoRepo.findAll();
        if (videos.isEmpty()) {
            return List.of(BotMessage.textOnly("Ещё нет добавленных видео."));
        }

        return videos.stream().map(v -> {
            InlineKeyboard kb = new InlineKeyboard(
                    List.of(
                            List.of(
                                    new InlineButton("Удалить", "delete:" + v.getVideoId())
                            )
                    )
            );
            return new BotMessage(v.getUrl(), kb, null);
        }).collect(Collectors.toList());
    }
}
