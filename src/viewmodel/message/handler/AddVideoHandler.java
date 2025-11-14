package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;
import model.domain.youtube.YouTubeService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AddVideoHandler implements MessageHandler {
    private final IRepository<Video, String> videoRepo;
    private final IUrlParser urlParser;
    private final YouTubeService youTubeService; // может быть null

    // Конструктор для совместимости с тестами
    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser) {
        this(videoRepo, urlParser, null);
    }

    // Новый конструктор — с интеграцией YouTube
    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser,
                           YouTubeService youTubeService) {
        this.videoRepo = videoRepo;
        this.urlParser = urlParser;
        this.youTubeService = youTubeService;
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        // Любое сообщение, не начинающееся с "/"
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

            // Сохраняем локально
            videoRepo.save(video);

            // Если YouTubeService передан — попробуем добавить в плейлист
            if (youTubeService != null) {
                try {
                    String playlistItemId = youTubeService.addVideoToPlaylist(video.getVideoId());
                    // Можно сохранять playlistItemId куда-нибудь, если нужно
                    return List.of("Видео добавлено: " + video.getUrl() +
                            "\nДобавлено в плейлист (playlistItemId): " + playlistItemId);
                } catch (IOException e) {
                    e.printStackTrace();
                    return List.of(
                            "Не удалось добавить в YouTube-плейлист: " + video.getUrl() + e.getMessage()
                    );
                }
            } else {
                return List.of(
                        "Не удалось добавить установить соединение с YouTube"
                );
            }

        } catch (IllegalArgumentException ex) {
            return List.of(ex.getMessage());
        }
    }
}
