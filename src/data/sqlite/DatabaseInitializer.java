package data.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    public static void init() {
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {

            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "userId INTEGER PRIMARY KEY," +
                            "platform TEXT NOT NULL" +
                            ")"
            );

            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS videos (" +
                            "videoId TEXT PRIMARY KEY," +
                            "url TEXT NOT NULL," +
                            "platform TEXT NOT NULL," +
                            "startTime INTEGER," +
                            "type TEXT," +
                            "timeAdded TEXT NOT NULL," +
                            "userAdded INTEGER," +
                            "playlistItemId TEXT" +
                            ")"
            );

            s.executeUpdate(
                    "ALTER TABLE videos ADD COLUMN playlistItemId TEXT"
            );

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("duplicate column name: playlistItemId")) {
                return;
            }
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private DatabaseInitializer() {}
}
