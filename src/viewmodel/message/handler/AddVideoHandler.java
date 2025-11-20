package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import services.youtube.IYouTubeService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AddVideoHandler implements MessageHandler {
    private final IRepository<Video, String> videoRepo;
    private final IUrlParser urlParser;
    private final IYouTubeService youTubeService; // may be a Noop implementation

    // Main constructor for Guice
    @Inject
    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser,
                           IYouTubeService youTubeService) {
        this.videoRepo = videoRepo;
        this.urlParser = urlParser;
        this.youTubeService = youTubeService;
    }

    // Convenience constructor for tests (keeps backwards compatibility)
    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser) {
        this(videoRepo, urlParser, null);
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        // Any message not starting with "/" and only if user exists
        return text != null && !text.startsWith("/") && userRepo.existsById(userId);
    }

    @Override
    public List<String> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        try {
            Video video = urlParser.parse(text);
            video.setUserAdded(userId);

            boolean exists = videoRepo.findAll().stream()
                    .map(Video::getUrl)
                    .collect(Collectors.toSet())
                    .contains(video.getUrl());

            if (exists) {
                return List.of("Видео уже существует: " + video.getUrl());
            }

            // Save locally
            videoRepo.save(video);

            // If YouTubeService is provided — try to add to playlist
            if (youTubeService != null) {
                try {
                    String playlistItemId = youTubeService.addVideoToPlaylist(video.getVideoId());
                    return List.of("Видео добавлено: " + video.getUrl() +
                            "\nДобавлено в плейлист (playlistItemId): " + playlistItemId);
                } catch (IOException e) {
                    e.printStackTrace();
                    return List.of("Не удалось добавить в YouTube-плейлист: " + video.getUrl() + " — " + e.getMessage());
                }
            } else {
                return List.of("Не удалось установить соединение с YouTube");
            }
        } catch (IllegalArgumentException ex) {
            return List.of(ex.getMessage());
        }
    }
}
