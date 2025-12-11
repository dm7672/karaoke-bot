package test.model.domain.entities;

import model.domain.entities.Video;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class VideoTest {

    @Test
    void constructorWithoutTimeAdded_setsTimeAddedNow() {
        Video video = new Video("http://example.com", "YouTube", "abc123", 10, "music");
        assertEquals("YouTube", video.getPlatform());
        assertEquals("abc123", video.getVideoId());
        assertEquals(Integer.valueOf(10), video.getStartTime());
        assertEquals("music", video.getType());
        assertEquals("http://example.com", video.getUrl());
        assertNotNull(video.getTimeAdded());
        LocalDateTime now = LocalDateTime.now();
        assertFalse(video.getTimeAdded().isAfter(now));
    }

    @Test
    void constructorWithTimeAdded_usesProvidedValues() {
        LocalDateTime customTime = LocalDateTime.of(2020, 1, 1, 12, 0);
        Video video = new Video("http://example.com", "Vimeo", "xyz789", 20, "clip", customTime, 42L, null);
        assertEquals("Vimeo", video.getPlatform());
        assertEquals("xyz789", video.getVideoId());
        assertEquals(Integer.valueOf(20), video.getStartTime());
        assertEquals("clip", video.getType());
        assertEquals("http://example.com", video.getUrl());
        assertEquals(customTime, video.getTimeAdded());
        assertEquals(Long.valueOf(42L), video.getUserAdded());
    }

    @Test
    void constructorWithNullTimeAdded_setsTimeAddedNow() {
        Video video = new Video("http://example.com", "TikTok", "id001", 30, "short", null, 99L, null);
        assertNotNull(video.getTimeAdded());
        assertEquals(Long.valueOf(99L), video.getUserAdded());
    }

    @Test
    void setUserAdded_changesValue() {
        Video video = new Video("http://example.com", "YouTube", "abc123", 10, "music");
        assertNull(video.getUserAdded());
        video.setUserAdded(123L);
        assertEquals(Long.valueOf(123L), video.getUserAdded());
    }

    @Test
    void constructorWithNullUserAdded_setsUserAddedNull() {
        LocalDateTime customTime = LocalDateTime.of(2022, 3, 1, 10, 0);
        Video video = new Video("http://example.com", "TikTok", "vid789", 25, "short", customTime, null, null);
        assertEquals("TikTok", video.getPlatform());
        assertEquals("vid789", video.getVideoId());
        assertEquals(Integer.valueOf(25), video.getStartTime());
        assertEquals("short", video.getType());
        assertEquals("http://example.com", video.getUrl());
        assertEquals(customTime, video.getTimeAdded());
        assertNull(video.getUserAdded());
    }

    @Test
    void constructor_setsAllFieldsCorrectly() {
        LocalDateTime customTime = LocalDateTime.of(2021, 5, 10, 15, 30);
        Video video = new Video("http://example.com", "YouTube", "vid123", 5, "music", customTime, 77L, "plId");
        assertEquals("http://example.com", video.getUrl());
        assertEquals("YouTube", video.getPlatform());
        assertEquals("vid123", video.getVideoId());
        assertEquals(Integer.valueOf(5), video.getStartTime());
        assertEquals("music", video.getType());
        assertEquals(customTime, video.getTimeAdded());
        assertEquals(Long.valueOf(77L), video.getUserAdded());
        assertEquals("plId", video.getPlaylistItemId());
    }
}
