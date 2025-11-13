package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;

import java.util.List;

public class NewUserHandler implements MessageHandler {
    private final String                    defaultPlatform;

    public NewUserHandler(IRepository<User, Long> userRepo,
                          String defaultPlatform) {
        this.defaultPlatform = defaultPlatform;
    }

    @Override
    public boolean canHandle(Long usedId, IRepository<User, Long> userRepo, String text) {
        // обрабатываем только если пользователя ещё нет
        return !userRepo.existsById(usedId);
    }

    @Override
    public List<String> handle(Long userId, IRepository<User, Long> userRepo, String text) {
        // сохраняем нового пользователя
        userRepo.save(new User(userId, defaultPlatform));

        // если первая команда – /start, выдаём особый текст
        if ("/start".equalsIgnoreCase(text.trim())) {
            return List.of(
                    "Добро пожаловать! Я бот для добавления YouTube-видео.",
                    "Чтобы узнать доступные команды, введите <userId> /help"
            );
        }

        // при любом другом первом сообщении тоже сохраняем и подсказываем /help
        return List.of(
                "Привет! Ты зарегистрирован как пользователь",
                "Для списка команд введи <userId> /help"
        );
    }
}
