package data.sqlite;

import model.domain.entities.Video;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SQLiteVideoRepository extends SQLiteRepository<Video, String> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    protected String getTableName() {
        return "videos";
    }

    @Override
    protected String getIdColumn() {
        return "videoId";
    }

    @Override
    protected Video mapRow(ResultSet rs) throws SQLException {
        String videoId = rs.getString("videoId");
        String url = rs.getString("url");
        String platform = rs.getString("platform");
        Integer startTime = rs.getObject("startTime") == null ? null : rs.getInt("startTime");
        String type = rs.getString("type");

        String timeAddedStr = rs.getString("timeAdded");
        LocalDateTime timeAdded = timeAddedStr != null ? LocalDateTime.parse(timeAddedStr, ISO) : LocalDateTime.now();

        Long userAdded = rs.getObject("userAdded") == null ? null : rs.getLong("userAdded");

        return new Video(url, platform, videoId, startTime, type, timeAdded, userAdded);
    }

    @Override
    protected PreparedStatement createInsertStatement(Connection c, Video entity) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
                "INSERT INTO videos (videoId, url, platform, startTime, type, timeAdded, userAdded) VALUES (?, ?, ?, ?, ?, ?, ?)"
        );
        ps.setString(1, entity.getVideoId());
        ps.setString(2, entity.getUrl());
        ps.setString(3, entity.getPlatform());
        if (entity.getStartTime() != null) ps.setInt(4, entity.getStartTime());
        else ps.setNull(4, Types.INTEGER);
        ps.setString(5, entity.getType());
        ps.setString(6, entity.getTimeAdded().format(ISO));
        if (entity.getUserAdded() != null) ps.setLong(7, entity.getUserAdded());
        else ps.setNull(7, Types.INTEGER);
        return ps;
    }

    @Override
    protected PreparedStatement createUpdateStatement(Connection c, Video entity) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
                "UPDATE videos SET url = ?, platform = ?, startTime = ?, type = ?, timeAdded = ?, userAdded = ? WHERE videoId = ?"
        );
        ps.setString(1, entity.getUrl());
        ps.setString(2, entity.getPlatform());
        if (entity.getStartTime() != null) ps.setInt(3, entity.getStartTime());
        else ps.setNull(3, Types.INTEGER);
        ps.setString(4, entity.getType());
        ps.setString(5, entity.getTimeAdded().format(ISO));
        if (entity.getUserAdded() != null) ps.setLong(6, entity.getUserAdded());
        else ps.setNull(6, Types.INTEGER);
        ps.setString(7, entity.getVideoId());
        return ps;
    }

    @Override
    protected String extractId(Video entity) {
        return entity.getVideoId();
    }
}
