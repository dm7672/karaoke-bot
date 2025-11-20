package data.sqlite;

import model.domain.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteUserRepository extends SQLiteRepository<User, Long> {

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected String getIdColumn() {
        return "userId";
    }

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        Long userId = rs.getLong("userId");
        String platform = rs.getString("platform");
        return new User(userId, platform);
    }

    @Override
    protected PreparedStatement createInsertStatement(Connection c, User entity) throws SQLException {
        PreparedStatement ps = c.prepareStatement("INSERT INTO users (userId, platform) VALUES (?, ?)");
        ps.setLong(1, entity.getUserId());
        ps.setString(2, entity.getPlatform());
        return ps;
    }

    @Override
    protected PreparedStatement createUpdateStatement(Connection c, User entity) throws SQLException {
        PreparedStatement ps = c.prepareStatement("UPDATE users SET platform = ? WHERE userId = ?");
        ps.setString(1, entity.getPlatform());
        ps.setLong(2, entity.getUserId());
        return ps;
    }

    @Override
    protected Long extractId(User entity) {
        return entity.getUserId();
    }
}
