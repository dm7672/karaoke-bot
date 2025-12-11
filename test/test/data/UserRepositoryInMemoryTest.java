package test.data;

import data.IRepository;
import data.InMemoryRepository;
import model.domain.entities.User;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryInMemoryTest {
    private IRepository<User, Long> repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository<>(User::getUserId);
    }

    @Test
    void findAllInitiallyEmpty() {
        List<User> all = repo.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void saveAndFindById() {
        User u = new User(42L, "vk");
        repo.save(u);

        User fetched = repo.findById(42L);
        assertNotNull(fetched);
        assertEquals(42L, fetched.getUserId());
        assertEquals("vk", fetched.getPlatform());
    }

    @Test
    void updateExisting() {
        User u = new User(7L, "yt");
        repo.save(u);

        User modified = new User(7L, "twitch");
        repo.update(modified);

        assertEquals("twitch", repo.findById(7L).getPlatform());
    }

    @Test
    void updateNonExistingThrows() {
        User u = new User(5L, "ig");
        assertThrows(NoSuchElementException.class, () -> repo.update(u));
    }

    @Test
    void deleteRemovesEntity() {
        User u = new User(3L, "fb");
        repo.save(u);
        assertNotNull(repo.findById(3L));

        repo.delete(3L);
        assertNull(repo.findById(3L));
    }

    @Test
    void existsByIdDefaultMethod() {
        // если в интерфейсе есть default existsById(ID)
        assertFalse(repo.existsById(1L));
        repo.save(new User(1L, "a"));
        assertTrue(repo.existsById(1L));
    }

    @Test
    void saveMultipleAndFindAll() {
        repo.save(new User(1L, "vk"));
        repo.save(new User(2L, "yt"));
        repo.save(new User(3L, "tg"));

        List<User> all = repo.findAll();
        assertEquals(3, all.size());
        assertTrue(all.stream().anyMatch(u -> u.getUserId() == 1L && u.getPlatform().equals("vk")));
        assertTrue(all.stream().anyMatch(u -> u.getUserId() == 2L && u.getPlatform().equals("yt")));
        assertTrue(all.stream().anyMatch(u -> u.getUserId() == 3L && u.getPlatform().equals("tg")));
    }

    @Test
    void deleteNonExistingDoesNothingOrThrows() {
        assertDoesNotThrow(() -> repo.delete(999L));
        assertNull(repo.findById(999L));
    }

    @Test
    void saveWithDuplicateIdOverwritesOrThrows() {
        User u1 = new User(10L, "vk");
        User u2 = new User(10L, "yt");

        repo.save(u1);
        repo.save(u2); // ожидаем, что второй перезапишет первого

        User fetched = repo.findById(10L);
        assertNotNull(fetched);
        assertEquals("yt", fetched.getPlatform());
    }

    @Test
    void findByIdNonExistingReturnsNull() {
        assertNull(repo.findById(12345L));
    }

    @Test
    void updateAfterDeleteThrows() {
        User u = new User(20L, "ok");
        repo.save(u);
        repo.delete(20L);

        assertThrows(NoSuchElementException.class, () -> repo.update(new User(20L, "new")));
    }

    @Test
    void saveAndDeleteMultiple() {
        repo.save(new User(30L, "a"));
        repo.save(new User(31L, "b"));
        repo.save(new User(32L, "c"));

        repo.delete(31L);

        List<User> all = repo.findAll();
        assertEquals(2, all.size());
        assertNull(repo.findById(31L));
        assertNotNull(repo.findById(30L));
        assertNotNull(repo.findById(32L));
    }
}
