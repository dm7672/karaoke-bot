package test.services.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import services.youtube.YouTubeService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeServiceTest {

    @Test
    void addVideoToPlaylist_returnsId() throws Exception {
        PlaylistItem mockResponse = new PlaylistItem();
        mockResponse.setId("playlistItem123");

        YouTube.PlaylistItems.Insert mockInsert = Mockito.mock(YouTube.PlaylistItems.Insert.class);
        Mockito.when(mockInsert.execute()).thenReturn(mockResponse);

        YouTube.PlaylistItems mockPlaylistItems = Mockito.mock(YouTube.PlaylistItems.class);
        Mockito.when(mockPlaylistItems.insert(Mockito.eq("snippet"), Mockito.any(PlaylistItem.class)))
                .thenReturn(mockInsert);

        YouTube mockYouTube = Mockito.mock(YouTube.class);
        Mockito.when(mockYouTube.playlistItems()).thenReturn(mockPlaylistItems);

        YouTubeService service = Mockito.mock(YouTubeService.class, Mockito.CALLS_REAL_METHODS);
        var youtubeField = YouTubeService.class.getDeclaredField("youtube");
        youtubeField.setAccessible(true);
        youtubeField.set(service, mockYouTube);
        var playlistField = YouTubeService.class.getDeclaredField("playlistId");
        playlistField.setAccessible(true);
        playlistField.set(service, "testPlaylist");

        String result = service.addVideoToPlaylist("video123");
        assertEquals("playlistItem123", result);
    }

    @Test
    void constructor_throwsException_whenPlaylistIdMissing() {
        Dotenv mockDotenv = Mockito.mock(Dotenv.class);
        Mockito.when(mockDotenv.get("YT_PLAYLIST_ID")).thenReturn(null);

        try (MockedStatic<Dotenv> dotenvStatic = Mockito.mockStatic(Dotenv.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(mockDotenv);
            assertThrows(IllegalStateException.class, YouTubeService::new);
        }
    }
}
