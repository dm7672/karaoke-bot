package test.services.youtube;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import services.youtube.YouTubeInitializer;
import services.youtube.YouTubeService;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeInitializerTest {

    @Test
    void init_returnsNull_whenPlaylistIdMissingOrBlank() {
        Dotenv mockDotenv = Mockito.mock(Dotenv.class);
        Mockito.when(mockDotenv.get("YT_PLAYLIST_ID")).thenReturn("");

        try (MockedStatic<Dotenv> dotenvStatic = Mockito.mockStatic(Dotenv.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(mockDotenv);
            YouTubeService service = YouTubeInitializer.init();
            assertNull(service);
        }
    }

    @Test
    void init_returnsService_whenPlaylistIdPresent() {
        Dotenv mockDotenv = Mockito.mock(Dotenv.class);
        Mockito.when(mockDotenv.get("YT_PLAYLIST_ID")).thenReturn("playlist123");

        try (MockedStatic<Dotenv> dotenvStatic = Mockito.mockStatic(Dotenv.class);
             MockedConstruction<YouTubeService> constructed = Mockito.mockConstruction(YouTubeService.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(mockDotenv);
            YouTubeService service = YouTubeInitializer.init();
            assertNotNull(service);
            assertSame(constructed.constructed().get(0), service);
        }
    }

}
