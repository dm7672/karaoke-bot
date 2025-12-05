package model.domain.entities;

import java.time.LocalDateTime;

public class Video {

    private final String platform;
    private final String videoId;
    private final Integer startTime;
    private final String type;
    private final String url;
    private final LocalDateTime timeAdded;

    private Long userAdded;
    private String playlistItemId;

    public Video(String url, String platform, String videoId, Integer startTime, String type) {
        this.platform = platform;
        this.videoId = videoId;
        this.startTime = startTime;
        this.type = type;
        this.url = url;
        this.timeAdded = LocalDateTime.now();
    }

    public Video(String url, String platform, String videoId, Integer startTime, String type,
                 LocalDateTime timeAdded, Long userAdded, String playlistItemId) {
        this.platform = platform;
        this.videoId = videoId;
        this.startTime = startTime;
        this.type = type;
        this.url = url;
        this.timeAdded = timeAdded != null ? timeAdded : LocalDateTime.now();
        this.userAdded = userAdded;
        this.playlistItemId = playlistItemId;
    }

    public String getPlatform() { return platform; }
    public String getVideoId() { return videoId; }
    public Integer getStartTime() { return startTime; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public LocalDateTime getTimeAdded() { return timeAdded; }
    public Long getUserAdded() { return userAdded; }
    public String getPlaylistItemId() { return playlistItemId; }

    public void setUserAdded(Long userId){ this.userAdded = userId; }
    public void setPlaylistItemId(String playlistItemId) { this.playlistItemId = playlistItemId; }
}
