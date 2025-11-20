package viewmodel.message.handler;

import data.IRepository;
import model.domain.entities.User;

import java.util.List;

public interface MessageHandler {
    /**
     * Определяет, может ли этот обработчик взять на себя данное сообщение.
     */
    boolean canHandle(Long userId, IRepository<User, Long> userRepo, String text);

    /**
     * Выполняет логику и возвращает список строк-ответов.
     */
    List<String> handle(Long userId, IRepository<User, Long> userRepo, String text);
}
