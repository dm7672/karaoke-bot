package data.sqlite;

import data.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class SQLiteRepository<T, ID> implements IRepository<T, ID> {

    protected abstract String getTableName();
    protected abstract T mapRow(ResultSet rs) throws SQLException;
    protected abstract PreparedStatement createInsertStatement(Connection c, T entity) throws SQLException;
    protected abstract PreparedStatement createUpdateStatement(Connection c, T entity) throws SQLException;
    protected abstract String getIdColumn();
    protected abstract ID extractId(T entity);

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<T> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T findById(ID id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumn() + " = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id) != null;
    }

    @Override
    public void save(T entity) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = createInsertStatement(conn, entity)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(T entity) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = createUpdateStatement(conn, entity)) {
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Update affected 0 rows for id: " + extractId(entity));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(ID id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumn() + " = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
