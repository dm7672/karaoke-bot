package viewmodel.message.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import data.IRepository;
import model.domain.entities.User;

import java.util.List;

public class NewUserHandler implements MessageHandler {
    private final String defaultPlatform;
    private final IRepository<User, Long> userRepoReference; // optional if you want to keep a reference

    @Inject
    public NewUserHandler(IRepository<User, Long> userRepo,
                          @Named("platform") String defaultPlatform) {
        this.defaultPlatform = defaultPlatform;
        this.userRepoReference = userRepo;
    }

    @Override
    public boolean canHandle(Long usedId, IRepository<User, Long> userRepo, String text) {
        // handle only when the user does not exist yet
        return !userRepo.existsById(usedId);
    }

    @Override
    public List<String> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        // save new user
        userRepo.save(new User(userId, defaultPlatform));

        // if first command is /start, return special text
        if ("/start".equalsIgnoreCase(text.trim())) {
            return List.of(
                    "Добро пожаловать! Я бот для добавления YouTube-видео.",
                    "Чтобы узнать доступные команды, введите /help"
            );
        }

        // for any other first message also register and suggest /help
        return List.of(
                "Привет! Ты зарегистрирован как пользователь",
                "Для списка команд введи /help"
        );
    }
}
