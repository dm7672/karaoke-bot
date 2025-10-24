package test.data;

import model.domain.entities.User;
import model.data.*;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryInMemoryTest {
    private IRepository<User, Integer> repo;

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
        User u = new User(42, "vk");
        repo.save(u);

        User fetched = repo.findById(42);
        assertNotNull(fetched);
        assertEquals(42, fetched.getUserId());
        assertEquals("vk", fetched.getPlatform());
    }

    @Test
    void updateExisting() {
        User u = new User(7, "yt");
        repo.save(u);

        User modified = new User(7, "twitch");
        repo.update(modified);

        assertEquals("twitch", repo.findById(7).getPlatform());
    }

    @Test
    void updateNonExistingThrows() {
        User u = new User(5, "ig");
        assertThrows(NoSuchElementException.class, () -> repo.update(u));
    }

    @Test
    void deleteRemovesEntity() {
        User u = new User(3, "fb");
        repo.save(u);
        assertNotNull(repo.findById(3));

        repo.delete(3);
        assertNull(repo.findById(3));
    }

    @Test
    void existsByIdDefaultMethod() {
        // если в интерфейсе есть default existsById(ID)
        assertFalse(repo.existsById(1));
        repo.save(new User(1, "a"));
        assertTrue(repo.existsById(1));
    }

    @Test
    void saveMultipleAndFindAll() {
        repo.save(new User(1, "vk"));
        repo.save(new User(2, "yt"));
        repo.save(new User(3, "tg"));

        List<User> all = repo.findAll();
        assertEquals(3, all.size());
        assertTrue(all.stream().anyMatch(u -> u.getUserId() == 1 && u.getPlatform().equals("vk")));
        assertTrue(all.stream().anyMatch(u -> u.getUserId() == 2 && u.getPlatform().equals("yt")));
        assertTrue(all.stream().anyMatch(u -> u.getUserId() == 3 && u.getPlatform().equals("tg")));
    }

    @Test
    void deleteNonExistingDoesNothingOrThrows() {
        assertDoesNotThrow(() -> repo.delete(999));
        assertNull(repo.findById(999));
    }

    @Test
    void saveWithDuplicateIdOverwritesOrThrows() {
        User u1 = new User(10, "vk");
        User u2 = new User(10, "yt");

        repo.save(u1);
        repo.save(u2); // ожидаем, что второй перезапишет первого

        User fetched = repo.findById(10);
        assertNotNull(fetched);
        assertEquals("yt", fetched.getPlatform());
    }

    @Test
    void findByIdNonExistingReturnsNull() {
        assertNull(repo.findById(12345));
    }

    @Test
    void updateAfterDeleteThrows() {
        User u = new User(20, "ok");
        repo.save(u);
        repo.delete(20);

        assertThrows(NoSuchElementException.class, () -> repo.update(new User(20, "new")));
    }

    @Test
    void saveAndDeleteMultiple() {
        repo.save(new User(30, "a"));
        repo.save(new User(31, "b"));
        repo.save(new User(32, "c"));

        repo.delete(31);

        List<User> all = repo.findAll();
        assertEquals(2, all.size());
        assertNull(repo.findById(31));
        assertNotNull(repo.findById(30));
        assertNotNull(repo.findById(32));
    }
}
