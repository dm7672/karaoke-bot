package test.data.sqlite;

import data.sqlite.*;
import model.domain.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteUserRepositoryTest {

    private SQLiteUserRepository repo;

    @BeforeEach
    void setUp() {
        DatabaseInitializer.init();

        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        repo = new SQLiteUserRepository();
    }

    @AfterEach
    void tearDown() {
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveAndFindById_andExists() {
        User u = new User(100L, "test-platform");
        repo.save(u);

        assertTrue(repo.existsById(100L), "saved user should exist");
        User found = repo.findById(100L);
        assertNotNull(found, "findById should return the saved user");
        assertEquals(100L, found.getUserId().longValue());
        assertEquals("test-platform", found.getPlatform());
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        User u1 = new User(1L, "p1");
        User u2 = new User(2L, "p2");
        repo.save(u1);
        repo.save(u2);

        List<User> all = repo.findAll();
        assertNotNull(all);
        assertEquals(2, all.size(), "should return two users");
        assertTrue(all.stream().anyMatch(u -> u.getUserId().equals(1L)));
        assertTrue(all.stream().anyMatch(u -> u.getUserId().equals(2L)));
    }

    @Test
    void update_modifiesExistingUser() {
        User u = new User(50L, "initial");
        repo.save(u);

        User updated = new User(50L, "changed");
        repo.update(updated);

        User fromDb = repo.findById(50L);
        assertNotNull(fromDb);
        assertEquals("changed", fromDb.getPlatform(), "platform should be updated");
    }

    @Test
    void delete_removesUser() {
        User u = new User(77L, "for-delete");
        repo.save(u);
        assertTrue(repo.existsById(77L));

        repo.delete(77L);
        assertFalse(repo.existsById(77L));
        assertNull(repo.findById(77L));
    }

    @Test
    void saveDuplicate_throwsRuntimeException() {
        User u = new User(3L, "a");
        repo.save(u);

        User dup = new User(3L, "b");
        Exception ex = assertThrows(RuntimeException.class, () -> repo.save(dup));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("unique") || msg.contains("constraint") || msg.contains("primary"),
                "Expected a constraint/primary key related error, got: " + ex.getMessage());
    }
}
