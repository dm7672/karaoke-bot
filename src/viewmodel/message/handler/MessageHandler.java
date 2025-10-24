package viewmodel.message.handler;

import model.data.IRepository;
import model.domain.entities.User;

import java.util.List;

public interface MessageHandler {
    /**
     * Определяет, может ли этот обработчик взять на себя данное сообщение.
     */
    boolean canHandle(Integer userId, IRepository<User, Integer> userRepo, String text);

    /**
     * Выполняет логику и возвращает список строк-ответов.
     */
    List<String> handle(Integer userId, IRepository<User, Integer> userRepo, String text);
}
