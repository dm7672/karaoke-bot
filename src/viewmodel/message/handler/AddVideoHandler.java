package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import io.github.cdimascio.dotenv.Dotenv;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import services.youtube.IYouTubeService;
import viewmodel.BotMessage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AddVideoHandler implements MessageHandler {
    private final IRepository<Video, String> videoRepo;
    private final IUrlParser urlParser;
    private final IYouTubeService youTubeService;

    @Inject
    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser,
                           IYouTubeService youTubeService) {
        this.videoRepo = videoRepo;
        this.urlParser = urlParser;
        this.youTubeService = youTubeService;
    }

    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser) {
        this(videoRepo, urlParser, null);
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        if (text == null || !userRepo.existsById(userId)) {
            return false;
        }
        String t = text.trim();
        if (t.startsWith("/")) {
            return false;
        }
        if (t.startsWith("delete:") || t.equals("send_url")) {
            return false;
        }
        return true;
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        try {
            Video video = urlParser.parse(text);
            video.setUserAdded(userId);

            boolean exists = videoRepo.findAll().stream()
                    .map(Video::getUrl)
                    .collect(Collectors.toSet())
                    .contains(video.getUrl());

            if (exists) {
                return List.of(BotMessage.textOnly("Видео уже существует: " + video.getUrl() + "\nВ плейлисте: " + "https://www.youtube.com/playlist?list="+ Dotenv.load().get("YT_PLAYLIST_ID")));
            }

            videoRepo.save(video);

            if (youTubeService != null) {
                try {
                    String playlistItemId = youTubeService.addVideoToPlaylist(video.getVideoId());
                    video.setPlaylistItemId(playlistItemId);
                    videoRepo.update(video);
                    return List.of(
                            BotMessage.textOnly("Видео добавлено: " + video.getUrl() + "\nВ плейлист: " + "https://www.youtube.com/playlist?list="+ Dotenv.load().get("YT_PLAYLIST_ID"))
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return List.of(BotMessage.textOnly("Не удалось добавить в YouTube-плейлист: " + video.getUrl() + " — " + e.getMessage()));
                }
            } else {
                return List.of(BotMessage.textOnly("Видео добавлено локально: " + video.getUrl() + "\nYouTube недоступен"));
            }
        } catch (IllegalArgumentException ex) {
            return List.of(BotMessage.textOnly(ex.getMessage()));
        }
    }
}
