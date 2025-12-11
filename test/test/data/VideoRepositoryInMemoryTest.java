package test.data;

import data.IRepository;
import data.InMemoryRepository;
import model.domain.entities.Video;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class VideoRepositoryInMemoryTest {
    private IRepository<Video, String> repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository<>(Video::getVideoId);
    }

    @Test
    void findAllInitiallyEmpty() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void saveAndFindById() {
        Video v = new Video("url", "yt", "vid123", 10, "clip");
        v.setUserAdded(99L);
        repo.save(v);

        Video fetched = repo.findById("vid123");
        assertNotNull(fetched);
        assertEquals("yt", fetched.getPlatform());
        assertEquals(99, fetched.getUserAdded());
    }

    @Test
    void updateNonExistingThrows() {
        Video v = new Video("u", "p", "x", 0, "t");
        assertThrows(NoSuchElementException.class, () -> repo.update(v));
    }

    @Test
    void deleteRemovesEntity() {
        Video v = new Video("url", "p", "z", 1, "t");
        repo.save(v);
        assertNotNull(repo.findById("z"));

        repo.delete("z");
        assertNull(repo.findById("z"));
    }

    @Test
    void saveMultipleAndFindAll() {
        repo.save(new Video("u1", "yt", "v1", 100, "clip"));
        repo.save(new Video("u2", "vk", "v2", 200, "short"));
        repo.save(new Video("u3", "tg", "v3", 300, "live"));

        List<Video> all = repo.findAll();
        assertEquals(3, all.size());
        assertTrue(all.stream().anyMatch(v -> v.getVideoId().equals("v1") && v.getPlatform().equals("yt")));
        assertTrue(all.stream().anyMatch(v -> v.getVideoId().equals("v2") && v.getPlatform().equals("vk")));
        assertTrue(all.stream().anyMatch(v -> v.getVideoId().equals("v3") && v.getPlatform().equals("tg")));
    }

    @Test
    void findByIdNonExistingReturnsNull() {
        assertNull(repo.findById("no_such_video"));
    }

    @Test
    void deleteNonExistingDoesNothingOrThrows() {
        assertDoesNotThrow(() -> repo.delete("ghost"));
        assertNull(repo.findById("ghost"));
    }

    @Test
    void saveWithDuplicateIdOverwrites() {
        Video v1 = new Video("url1", "yt", "dup", 10, "clip");
        Video v2 = new Video("url2", "vk", "dup", 20, "short");

        repo.save(v1);
        repo.save(v2);

        Video fetched = repo.findById("dup");
        assertNotNull(fetched);
        assertEquals("url2", fetched.getUrl());
        assertEquals("vk", fetched.getPlatform());
    }

    @Test
    void updateAfterDeleteThrows() {
        Video v = new Video("url", "yt", "gone", 15, "clip");
        repo.save(v);
        repo.delete("gone");

        assertThrows(NoSuchElementException.class, () -> repo.update(new Video("u", "p", "gone", 0, "t")));
    }

    @Test
    void existsByIdDefaultMethod() {
        assertFalse(repo.existsById("abc"));
        repo.save(new Video("url", "yt", "abc", 5, "clip"));
        assertTrue(repo.existsById("abc"));
    }
}
