package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;
import model.domain.parcer.IUrlParser;

import java.util.List;
import java.util.stream.Collectors;

public class AddVideoHandler implements MessageHandler {
    private final IRepository<Video, String> videoRepo;
    private final IUrlParser urlParser;

    public AddVideoHandler(IRepository<Video, String> videoRepo,
                           IUrlParser urlParser) {
        this.videoRepo = videoRepo;
        this.urlParser = urlParser;
    }

    @Override
    public boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text) {
        // Любое сообщение, не начинающееся с "/"
        return !text.startsWith("/") && userRepo.existsById(userId);
    }

    @Override
    public List<String> handle(Long userId,IRepository<User, Long> userRepo, String text) {
        try {
            // Выбросит IllegalArgumentException с нужным сообщением,
            // если URL некорректен или не YouTube
            Video video = urlParser.parse(text);
            video.setUserAdded(userId);

            // Проверяем дубликат по URL
            boolean exists = videoRepo.findAll().stream()
                    .map(Video::getUrl)
                    .collect(Collectors.toSet())
                    .contains(video.getUrl());

            if (exists) {
                return List.of("Видео уже существует: " + video.getUrl());
            }

            videoRepo.save(video);
            return List.of("Видео добавлено: " + video.getUrl());

        } catch (IllegalArgumentException ex) {
            // возвращаем именно текст исключения из парсера
            return List.of(ex.getMessage());
        }
    }
}