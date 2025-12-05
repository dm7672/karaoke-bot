package test.data.sqlite;

import data.sqlite.*;
import model.domain.entities.Video;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteVideoRepositoryTest {

    private SQLiteVideoRepository repo;

    @BeforeEach
    void setUp() {
        DatabaseInitializer.init();

        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM videos");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        repo = new SQLiteVideoRepository();
    }

    @AfterEach
    void tearDown() {
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM videos");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void saveAndFindById_andExists() {
        Video v = new Video("https://youtu.be/vid1", "YouTube", "vid1", 10, "watch");
        v.setUserAdded(123L);
        repo.save(v);

        assertTrue(repo.existsById("vid1"), "saved video should exist");

        Video found = repo.findById("vid1");
        assertNotNull(found, "findById should return the saved video");
        assertEquals("vid1", found.getVideoId());
        assertEquals("https://youtu.be/vid1", found.getUrl());
        assertEquals("YouTube", found.getPlatform());
        assertEquals(Integer.valueOf(10), found.getStartTime());
        assertEquals("watch", found.getType());
        assertEquals(123L, found.getUserAdded().longValue());
        assertNotNull(found.getTimeAdded(), "timeAdded should be present");
    }

    @Test
    void findAll_returnsAllSavedVideos() {
        Video a = new Video("https://youtu.be/a", "YouTube", "a", null, "shorts");
        Video b = new Video("https://youtu.be/b", "YouTube", "b", 5, "watch");
        repo.save(a);
        repo.save(b);

        List<Video> all = repo.findAll();
        assertNotNull(all);
        assertEquals(2, all.size(), "should return two videos");
        assertTrue(all.stream().anyMatch(v -> "a".equals(v.getVideoId())));
        assertTrue(all.stream().anyMatch(v -> "b".equals(v.getVideoId())));
    }

    @Test
    void update_modifiesExistingVideo() {
        Video v = new Video("https://youtu.be/upd", "YouTube", "upd", null, "short");
        repo.save(v);
        v = new Video("https://youtu.be/upd-new", "YouTube", "upd", 42, "watch");
        v.setUserAdded(999L);
        repo.update(v);

        Video fromDb = repo.findById("upd");
        assertNotNull(fromDb);
        assertEquals("https://youtu.be/upd-new", fromDb.getUrl());
        assertEquals(Integer.valueOf(42), fromDb.getStartTime());
        assertEquals(999L, fromDb.getUserAdded().longValue());
    }

    @Test
    void delete_removesVideo() {
        Video v = new Video("https://youtu.be/del", "YouTube", "del", null, "watch");
        repo.save(v);
        assertTrue(repo.existsById("del"));

        repo.delete("del");
        assertFalse(repo.existsById("del"));
        assertNull(repo.findById("del"));
    }

    @Test
    void saveDuplicate_throwsRuntimeException() {
        Video v = new Video("https://youtu.be/x", "YouTube", "x", null, "watch");
        repo.save(v);

        Video dup = new Video("https://youtu.be/x2", "YouTube", "x", null, "watch");
        Exception ex = assertThrows(RuntimeException.class, () -> repo.save(dup));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("constraint") || msg.contains("unique") || msg.contains("primary"),
                "Expected a constraint/primary key related error, got: " + ex.getMessage());
    }

    @Test
    void startTimeNullAndNonNullHandled() {
        Video nullStart = new Video("https://youtu.be/ns", "YouTube", "ns", null, "shorts");
        Video withStart = new Video("https://youtu.be/ws", "YouTube", "ws", 7, "watch");
        repo.save(nullStart);
        repo.save(withStart);

        Video a = repo.findById("ns");
        Video b = repo.findById("ws");
        assertNotNull(a);
        assertNull(a.getStartTime(), "startTime should be null when saved as null");
        assertNotNull(b.getStartTime(), "startTime should be present when saved");
        assertEquals(Integer.valueOf(7), b.getStartTime());
    }

    @Test
    void timeAdded_persistedAndParsed() {
        Video v = new Video("https://youtu.be/time", "YouTube", "time", null, "watch");
        repo.save(v);

        Video fromDb = repo.findById("time");
        assertNotNull(fromDb);
        LocalDateTime saved = fromDb.getTimeAdded();
        assertNotNull(saved, "timeAdded must be present");
        LocalDateTime now = LocalDateTime.now();
        assertFalse(saved.isAfter(now.plusSeconds(1)), "timeAdded should not be in the future");
        assertFalse(saved.isBefore(now.minusMinutes(2)), "timeAdded should be recent (within 2 minutes)");
    }

    @Test
    void playlistItemId_persistedAndLoaded() {
        Video v = new Video("https://youtu.be/pl", "YouTube", "pl", null, "watch");
        v.setUserAdded(55L);
        v.setPlaylistItemId("pl-xyz");
        repo.save(v);

        Video fromDb = repo.findById("pl");
        assertNotNull(fromDb);
        assertEquals("pl-xyz", fromDb.getPlaylistItemId());
        assertEquals(Long.valueOf(55L), fromDb.getUserAdded());
    }

    @Test
    void update_nonExisting_throwsRuntimeException() {
        Video v = new Video("https://youtu.be/no", "YouTube", "no", null, "watch");
        v.setUserAdded(1L);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> repo.update(v));
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("0") || msg.contains("affected") || msg.contains("no rows"),
                "Expected message indicating 0 rows affected, got: " + ex.getMessage());
    }

    @Test
    void mapRow_handlesNullUserAddedAndNullPlaylistItem() {
        Video v = new Video("https://youtu.be/nulls", "YouTube", "nulls", null, "short");
        repo.save(v);

        Video fromDb = repo.findById("nulls");
        assertNotNull(fromDb);
        assertNull(fromDb.getUserAdded(), "userAdded should be null when not set");
        assertNull(fromDb.getPlaylistItemId(), "playlistItemId should be null when not set");
    }

    @Test
    void timeAdded_customValue_persisted() {
        LocalDateTime custom = LocalDateTime.of(2000, 1, 1, 0, 0);
        Video v = new Video("https://youtu.be/custom", "YouTube", "custom", null, "watch", custom, 7L, "pl");
        repo.save(v);

        Video fromDb = repo.findById("custom");
        assertNotNull(fromDb);
        assertEquals(custom, fromDb.getTimeAdded(), "custom timeAdded should be preserved");
    }

    @Test
    void findById_returnsNullForMissing() {
        assertNull(repo.findById("no-such-id"), "findById should return null for missing id");
        assertFalse(repo.existsById("no-such-id"), "existsById should be false for missing id");
    }

    @Test
    void save_withPlaylistItemId_andNullUserAdded() {
        Video v = new Video("https://youtu.be/plonly", "YouTube", "plonly", null, "watch");
        v.setPlaylistItemId("only-pl");
        repo.save(v);

        Video fromDb = repo.findById("plonly");
        assertNotNull(fromDb);
        assertEquals("only-pl", fromDb.getPlaylistItemId());
        assertNull(fromDb.getUserAdded());
    }

    @Test
    void multipleDeletes_doNotThrowAndIdempotent() {
        Video v = new Video("https://youtu.be/multi", "YouTube", "multi", null, "watch");
        repo.save(v);
        assertTrue(repo.existsById("multi"));

        repo.delete("multi");
        assertFalse(repo.existsById("multi"));

        assertDoesNotThrow(() -> repo.delete("multi"));
        assertFalse(repo.existsById("multi"));
    }
}
