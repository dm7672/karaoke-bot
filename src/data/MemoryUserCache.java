package data;

import model.domain.entities.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserCache {
    private static final Map<Long, User> cache = new ConcurrentHashMap<>();

    public static User getOrCreate(Long id, String platform) {
        return cache.computeIfAbsent(id, k -> new User(id, platform));
    }

    public static void put(User user) {
        cache.put(user.getUserId(), user);
    }
}
