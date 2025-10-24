package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;
import model.domain.entities.Video;

import java.util.List;
import java.util.stream.Collectors;

public class MyVideosHandler implements MessageHandler {
private final IRepository<Video, String> videoRepo;
public MyVideosHandler(IRepository<Video, String> videoRepo ) {
    this.videoRepo = videoRepo;
}

@Override
public boolean canHandle(Integer userId, IRepository<User, Integer> userRepo, String text) {
    return "/MyVideos".equalsIgnoreCase(text.trim());
}

@Override
public List<String> handle(Integer userId,IRepository<User, Integer> userRepo, String text) {
    List<String> urls = videoRepo.findAll().stream()
            .filter(v -> userId.equals(v.getUserAdded()))
            .map(Video::getUrl)
            .collect(Collectors.toList());
    return urls.isEmpty()
            ? List.of("У вас ещё нет добавленных видео.")
            : urls;
}
}
