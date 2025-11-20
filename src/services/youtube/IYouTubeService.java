package services.youtube;

import java.io.IOException;

public interface IYouTubeService {
    /**
     * Добавляет видео в плейлист. Возвращает id созданного PlaylistItem.
     */
    public String addVideoToPlaylist(String videoId) throws IOException;
}
