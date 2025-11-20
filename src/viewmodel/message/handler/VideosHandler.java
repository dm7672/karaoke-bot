package viewmodel.message.handler;

import com.google.inject.Inject;
import data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;

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
    public List<String> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        List<String> urls = videoRepo.findAll().stream()
                .map(Video::getUrl)
                .collect(Collectors.toList());
        return urls.isEmpty()
                ? List.of("Ещё нет добавленных видео.")
                : urls;
    }
}
