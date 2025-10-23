package model.domain.parcer;

import model.domain.entities.Video;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeUrlParser implements IUrlParser {

    @Override
    public boolean isValid(String url){
        return url.contains("youtube.com") || url.contains("youtu.be");
    }

    @Override
    public Video parse(String url) throws IllegalArgumentException {
        try{
            URI u = new URI(url); // тк URL deprecated
            if (!isValid(url)) {
                throw new IllegalArgumentException("Ссылка не принадлежит Ютубу: " + url);
            }
            String host = u.getHost();
            String path = u.getPath();
            String query = u.getQuery();

            String videoId = null;
            String type = null;
            Integer startTime = null;

            // Определяет тип и ID
            if (path.equals("/watch") && query != null && query.contains("v=")) {
                videoId = extractParam(query, "v");
                type = "watch";
            } else if (host.contains("youtu.be")) {
                videoId = path.substring(1);
                type = "shortlink";
            } else if (path.startsWith("/shorts/")) {
                videoId = path.substring("/shorts/".length());
                type = "shorts";
            } else if (path.startsWith("/embed/")) {
                videoId = path.substring("/embed/".length());
                type = "embed";
            }

            // Парсит время начала видео
            if (query != null) {
                String tParam = extractParam(query, "t");
                String startParam = extractParam(query, "start");
                if (tParam != null) startTime = parseTimeToSeconds(tParam);
                else if (startParam != null) startTime = parseTimeToSeconds(startParam);
            }

            if (videoId == null || videoId.isEmpty()) {
                throw new IllegalArgumentException("Нельзя получить id видео из: " + url);
            }

            return new Video(url,"YouTube", videoId, startTime, type);

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Неисправная ссылка: " + url);
        }
    }

    // Вспомогательные
    private String extractParam(String query, String key) {
        Pattern p = Pattern.compile("(^|&)" + key + "=([^&]+)");
        Matcher m = p.matcher(query);
        if (m.find()) return m.group(2);
        return null;
    }

    private int parseTimeToSeconds(String t) {
        if (t.matches("\\d+")) return Integer.parseInt(t);
        int seconds = 0;
        Matcher m = Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?").matcher(t);
        if (m.matches()) {
            if (m.group(1) != null) seconds += Integer.parseInt(m.group(1)) * 3600;
            if (m.group(2) != null) seconds += Integer.parseInt(m.group(2)) * 60;
            if (m.group(3) != null) seconds += Integer.parseInt(m.group(3));
        }
        return seconds;
    }
}
