package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import services.youtube.IYouTubeService;
import viewmodel.BotMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteVideoHandler implements MessageHandler {

    private final IRepository<Video, String> videoRepo;
    private final IUrlParser urlParser;
    private final IYouTubeService youTubeService;

    @Inject
    public DeleteVideoHandler(IRepository<Video, String> videoRepo,
                              IUrlParser urlParser,
                              IYouTubeService youTubeService) {
        this.videoRepo = videoRepo;
        this.urlParser = urlParser;
        this.youTubeService = youTubeService;
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        if (text == null || !userRepo.existsById(userId)) {
            return false;
        }
        String t = text.trim();
        return t.startsWith("/Delete") || t.startsWith("delete:") || urlParser.isValid(t);
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        String trimmed = text.trim();
        String arg;

        if (urlParser.isValid(trimmed)) {
            arg = trimmed;
        }
        else if (trimmed.startsWith("delete:")) {
            arg = trimmed.substring("delete:".length());
        }
        else {
            String[] parts = trimmed.split("\\s+", 2);
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                return List.of(BotMessage.textOnly("Использование: /Delete <videoId или URL>"));
            }
            arg = parts[1].trim();
        }

        Video targetVideo = null;

        if (urlParser.isValid(arg)) {
            try {
                Video parsed = urlParser.parse(arg);
                targetVideo = videoRepo.findById(parsed.getVideoId());
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (targetVideo == null) {
            targetVideo = videoRepo.findById(arg);
        }

        if (targetVideo == null) {
            return List.of(BotMessage.textOnly("Видео не найдено: " + arg));
        }

        if (!userId.equals(targetVideo.getUserAdded())) {
            return List.of(BotMessage.textOnly("Вы не можете удалить чужое видео"));
        }

        List<BotMessage> messages = new ArrayList<>();

        if (youTubeService != null && targetVideo.getPlaylistItemId() != null) {
            try {
                youTubeService.removeVideoFromPlaylist(targetVideo.getPlaylistItemId());
            } catch (IOException e) {
                e.printStackTrace();
                messages.add(BotMessage.textOnly("Не удалось удалить из YouTube-плейлиста: " + e.getMessage()));
            }
        }

        videoRepo.delete(targetVideo.getVideoId());
        messages.add(BotMessage.textOnly("Видео удалено: " + targetVideo.getUrl()));

        return messages;
    }
}
