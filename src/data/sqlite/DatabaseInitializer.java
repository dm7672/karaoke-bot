package data.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    public static void init() {
        try (Connection c = DatabaseConfig.getConnection();
             Statement s = c.createStatement()) {

            // users
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "userId INTEGER PRIMARY KEY," +
                            "platform TEXT NOT NULL" +
                            ")"
            );

            // videos
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS videos (" +
                            "videoId TEXT PRIMARY KEY," +
                            "url TEXT NOT NULL," +
                            "platform TEXT NOT NULL," +
                            "startTime INTEGER," +
                            "type TEXT," +
                            "timeAdded TEXT NOT NULL," +   // ISO-8601 string
                            "userAdded INTEGER" +
                            ")"
            );

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private DatabaseInitializer() {}
}
