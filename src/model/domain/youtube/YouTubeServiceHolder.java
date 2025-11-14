package model.domain.youtube;

public final class YouTubeServiceHolder {
    private static volatile YouTubeService instance;

    private YouTubeServiceHolder() { }

    public static YouTubeService get() {
        return instance;
    }

    public static void set(YouTubeService service) {
        instance = service;
    }

    public static void clear() {
        instance = null;
    }

    public static boolean isPresent() {
        return instance != null;
    }
}
