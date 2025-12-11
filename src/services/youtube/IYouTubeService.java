package services.youtube;

import java.io.IOException;

public interface IYouTubeService {
    String addVideoToPlaylist(String videoId) throws IOException;
    void removeVideoFromPlaylist(String playlistItemId) throws IOException;
}
