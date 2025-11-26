package test.data.sqlite;

import data.sqlite.DatabaseInitializer;
import data.sqlite.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerTest {

    @BeforeEach
    void resetDatabase() throws SQLException {
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("DROP TABLE IF EXISTS users");
            s.executeUpdate("DROP TABLE IF EXISTS videos");
        }
    }

    @Test
    void init_createsUsersTable() throws SQLException {
        DatabaseInitializer.init();
        try (Connection c = DatabaseConfig.getConnection();
             ResultSet rs = c.getMetaData().getTables(null, null, "users", null)) {
            assertTrue(rs.next());
        }
    }

    @Test
    void init_createsVideosTable() throws SQLException {
        DatabaseInitializer.init();
        try (Connection c = DatabaseConfig.getConnection();
             ResultSet rs = c.getMetaData().getTables(null, null, "videos", null)) {
            assertTrue(rs.next());
        }
    }

    @Test
    void init_isIdempotent() throws SQLException {
        DatabaseInitializer.init();
        DatabaseInitializer.init();
        try (Connection c = DatabaseConfig.getConnection();
             ResultSet rsUsers = c.getMetaData().getTables(null, null, "users", null);
             ResultSet rsVideos = c.getMetaData().getTables(null, null, "videos", null)) {
            assertTrue(rsUsers.next());
            assertTrue(rsVideos.next());
        }
    }
}
