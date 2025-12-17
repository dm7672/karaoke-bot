package viewmodel.message.handler;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import data.IRepository;
import model.domain.entities.User;
import viewmodel.BotMessage;
import viewmodel.ReplyKeyboard;

import java.util.List;

public class NewUserHandler implements MessageHandler {
    private final String defaultPlatform;
    private final IRepository<User, Long> userRepoReference;

    @Inject
    public NewUserHandler(IRepository<User, Long> userRepo,
                          @Named("platform") String defaultPlatform) {
        this.defaultPlatform = defaultPlatform;
        this.userRepoReference = userRepo;
    }

    @Override
    public boolean canHandle(Long usedId, IRepository<User, Long> userRepo, String text) {
        return !userRepo.existsById(usedId);
    }

    @Override
    public List<BotMessage> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        userRepo.save(new User(userId, defaultPlatform));

        ReplyKeyboard keyboard = ReplyKeyboard.simple(
                List.of("/Videos", "/MyVideos", "/help")
        );

        if ("/start".equalsIgnoreCase(text.trim())) {
            BotMessage msg = new BotMessage(
                    String.join("\n",
                            "Добро пожаловать! Я бот для добавления YouTube-видео.",
                            "Чтобы узнать доступные команды, введите /help или нажмите на кнопку команды"
                    ),
                    null,
                    keyboard
            );
            return List.of(msg);
        }

        BotMessage msg = new BotMessage(
                String.join("\n",
                        "Добро пожаловать! Я бот для добавления YouTube-видео.",
                        "Чтобы узнать доступные команды, введите /help или нажмите на кнопку команды"
                ),
                null,
                keyboard
        );
        return List.of(msg);
    }
}
