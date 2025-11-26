package test.model.domain.parcer;


import model.domain.entities.Video;
import model.domain.parcer.YouTubeUrlParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeUrlParserTest {

    private final YouTubeUrlParser parser = new YouTubeUrlParser();

    @Test
    @DisplayName("Анализирует стандартный URL-адрес")
    void testWatchUrl() throws Exception {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        Video data = parser.parse(url);

        assertEquals("dQw4w9WgXcQ", data.getVideoId());
        assertEquals("watch", data.getType());
        assertNull(data.getStartTime());
    }

    @Test
    @DisplayName("Парсит URL-адрес шортсов")
    void testShortUrlWithTime() throws Exception {
        String url = "https://youtu.be/dQw4w9WgXcQ?t=1m30s";
        Video data = parser.parse(url);

        assertEquals("dQw4w9WgXcQ", data.getVideoId());
        assertEquals("shortlink", data.getType());
        assertEquals(90, data.getStartTime());
    }

    @Test
    @DisplayName("Анализирует URL-адрес коротких видеороликов YouTube")
    void testShortsUrl() throws Exception {
        String url = "https://www.youtube.com/shorts/abc123xyz";
        Video data = parser.parse(url);

        assertEquals("abc123xyz", data.getVideoId());
        assertEquals("shorts", data.getType());
    }

    @Test
    @DisplayName("Отклоняет URL, не относящийся к YouTube")
    void testInvalidHost() {
        assertThrows(IllegalArgumentException.class, () -> {
            String url = "https://example.com/watch?v=abc";
            parser.parse(url);
        });
    }

    @Test
    @DisplayName("Отклоняет URL-адрес YouTube без идентификатора видео")
    void testNoVideoId() {
        assertThrows(IllegalArgumentException.class, () -> {
            String url = "https://www.youtube.com/watch?v=";
            parser.parse(url);
        });
    }

    @Test
    @DisplayName("Отклоняет искаженный URL-адрес")
    void testMalformedUrl() {
        assertThrows(IllegalArgumentException.class, () -> {
            String url ="ht!tp://not_valid_url://www.youtube.com/";
            parser.parse(url);
        });
    }

    @Test
    @DisplayName("Парсит URL с параметром start в секундах")
    void testWatchUrlWithStartSeconds() throws Exception {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&start=45";
        Video data = parser.parse(url);

        assertEquals("dQw4w9WgXcQ", data.getVideoId());
        assertEquals("watch", data.getType());
        assertEquals(45, data.getStartTime());
    }

    @Test
    @DisplayName("Парсит URL с параметром t в секундах")
    void testWatchUrlWithTSeconds() throws Exception {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=75s";
        Video data = parser.parse(url);

        assertEquals("dQw4w9WgXcQ", data.getVideoId());
        assertEquals("watch", data.getType());
        assertEquals(75, data.getStartTime());
    }

    @Test
    @DisplayName("Парсит короткий URL без параметров")
    void testShortUrlNoParams() throws Exception {
        String url = "https://youtu.be/xyz987";
        Video data = parser.parse(url);

        assertEquals("xyz987", data.getVideoId());
        assertEquals("shortlink", data.getType());
        assertNull(data.getStartTime());
    }

    @Test
    @DisplayName("Парсит embed URL")
    void testEmbedUrl() throws Exception {
        String url = "https://www.youtube.com/embed/dQw4w9WgXcQ";
        Video data = parser.parse(url);

        assertEquals("dQw4w9WgXcQ", data.getVideoId());
        assertEquals("embed", data.getType());
        assertNull(data.getStartTime());
    }

    @Test
    @DisplayName("Парсит URL с дополнительными параметрами")
    void testUrlWithExtraParams() throws Exception {
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&feature=share&ab_channel=RickAstley";
        Video data = parser.parse(url);

        assertEquals("dQw4w9WgXcQ", data.getVideoId());
        assertEquals("watch", data.getType());
        assertNull(data.getStartTime());
    }
}
