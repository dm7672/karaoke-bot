package services.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class YouTubeService implements IYouTubeService {
    // resource inside JAR (keep leading slash)
    private static final String CREDENTIALS_FILE_PATH = "/client_secret_192151238148-2dfbb47a6eemfifr2pc4l48rjahoqpu0.apps.googleusercontent.com.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Default tokens directory: ~/.karaoke_bot_tokens, can be overridden by env YT_TOKENS_DIR
    private static final String TOKENS_DIRECTORY_PATH =
            Dotenv.load().get("YT_TOKENS_DIR", getJarDirectory() + "/tokens");

    private final YouTube youtube;
    private final String playlistId;

    public YouTubeService() throws GeneralSecurityException, IOException, IllegalStateException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        this.youtube = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("AppName")
                .build();
        Dotenv dotenv = Dotenv.load();
        this.playlistId = dotenv.get("YT_PLAYLIST_ID");
        if (playlistId == null) {
            throw new IllegalStateException("YT_PLAYLIST_ID не указан");
        }
    }

    private static Credential authorize(NetHttpTransport httpTransport) throws IOException {
        // выбор режима авторизации: device или local-server (по умолчанию local-server)
        String method = System.getenv().getOrDefault("YT_OAUTH_METHOD", "device");
        if ("device".equalsIgnoreCase(method)) {
            return authorizeDeviceFlow(httpTransport);
        }
        return authorizeWithLocalServer(httpTransport);
    }

    private static Credential authorizeWithLocalServer(NetHttpTransport httpTransport) throws IOException {
        InputStream in = YouTubeService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Ресурс не найден " + CREDENTIALS_FILE_PATH + ".\n" +
                    "Поместите client_secret_*.json в resources или задайте правильный путь.");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        List<String> scopes = Collections.singletonList(YouTubeScopes.YOUTUBE);

        File tokensDir = new File(TOKENS_DIRECTORY_PATH);
        if (!tokensDir.exists()) {
            tokensDir.mkdirs();
        }
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(tokensDir);
        com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow flow =
                new com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, scopes)
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("offline")
                        .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static Credential authorizeDeviceFlow(NetHttpTransport httpTransport) throws IOException {
        // Загружаем client_secret
        InputStream in = YouTubeService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            // fallback: попробовать путь из env
            String alt = System.getenv("YT_CLIENT_SECRET_PATH");
            if (alt == null || alt.isBlank()) {
                alt = "client_secret_192151238148-2dfbb47a6eemfifr2pc4l48rjahoqpu0.apps.googleusercontent.com.json";
            }
            File altFile = new File(alt);
            if (altFile.exists()) {
                in = new FileInputStream(altFile);
                System.out.println("Loaded client_secret from file: " + altFile.getAbsolutePath());
            }
        }
        if (in == null) {
            throw new FileNotFoundException("Ресурс не найден " + CREDENTIALS_FILE_PATH + ". Попробуйте положить client_secret_*.json рядом с jar и/или задать YT_CLIENT_SECRET_PATH.");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        String clientId = clientSecrets.getDetails().getClientId();
        String clientSecret = clientSecrets.getDetails().getClientSecret();
        List<String> scopes = Collections.singletonList(YouTubeScopes.YOUTUBE);

        // Сначала создаём каталог токенов и dataStore / flow — чтобы можно было loadCredential
        File tokensDir = new File(TOKENS_DIRECTORY_PATH);
        if (!tokensDir.exists()) {
            tokensDir.mkdirs();
        }
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(tokensDir);

        com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow flow =
                new com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, scopes)
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("offline")
                        .build();

        // Попытка загрузить уже существующую credential
        Credential existing = flow.loadCredential("user");
        if (existing != null) {
            // Попробуем обновить токен, если нужен refresh (чтобы удостовериться, что credential рабочий)
            try {
                boolean refreshed = false;
                if (existing.getExpiresInSeconds() != null && existing.getExpiresInSeconds() <= 60) {
                    refreshed = existing.refreshToken();
                }
                System.out.println("Found existing credential (refreshable=" + (existing.getRefreshToken() != null) + ", refreshed=" + refreshed + ")");
            } catch (Exception e) {
                System.err.println("Warning: unable to refresh existing credential: " + e.getMessage());
            }
            return existing;
        }

        // Если credential нет — запускаем device flow (как раньше)
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();

        GenericUrl deviceUrl = new GenericUrl("https://oauth2.googleapis.com/device/code");
        Map<String, String> deviceParams = new HashMap<>();
        deviceParams.put("client_id", clientId);
        deviceParams.put("scope", String.join(" ", scopes));
        UrlEncodedContent deviceContent = new UrlEncodedContent(deviceParams);

        GenericJson deviceJson;
        try {
            HttpResponse deviceResponse = requestFactory.buildPostRequest(deviceUrl, deviceContent).execute();

            System.out.println("device_code response status: " + deviceResponse.getStatusCode());
            System.out.println("device_code response content-type: " + deviceResponse.getContentType());

            String body = null;
            try {
                body = deviceResponse.parseAsString();
            } catch (Exception parseEx) {
                InputStream is = deviceResponse.getContent();
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    body = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                }
            }

            if (body == null || body.isBlank()) {
                throw new IOException("Пустой ответ от device endpoint (status=" + deviceResponse.getStatusCode() + ")");
            }

            com.google.api.client.json.JsonObjectParser jsonParser =
                    new com.google.api.client.json.JsonObjectParser(JSON_FACTORY);
            deviceJson = jsonParser.parseAndClose(
                    new ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                    java.nio.charset.StandardCharsets.UTF_8,
                    GenericJson.class
            );
        } catch (HttpResponseException e) {
            String body = e.getContent();
            System.err.println("HTTP error requesting device_code: status=" + e.getStatusCode() + " body=" + body);
            System.err.println("clientId=" + clientId + " ; clientSecret present? " + (clientSecret != null && !clientSecret.isBlank()));
            throw new IOException("Ошибка при запросе device_code: " + body, e);
        }

        String deviceCode = (String) deviceJson.get("device_code");
        String userCode = (String) deviceJson.get("user_code");
        String verificationUrl = deviceJson.containsKey("verification_url")
                ? (String) deviceJson.get("verification_url")
                : (String) deviceJson.get("verification_uri");
        int interval = deviceJson.containsKey("interval")
                ? ((Number) deviceJson.get("interval")).intValue()
                : 5;

        System.out.println();
        System.out.println("Для завершения авторизации откройте в браузере: " + verificationUrl);
        System.out.println("И введите этот код: " + userCode);
        System.out.println("(Можно открыть на любом устройстве с браузером)");
        System.out.println("Ожидаем подтверждения...");
        System.out.println();

        GenericUrl tokenUrl = new GenericUrl("https://oauth2.googleapis.com/token");
        TokenResponse tokenResponse = null;
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for device authorization", e);
            }

            Map<String, String> tokenParams = new HashMap<>();
            tokenParams.put("client_id", clientId);
            if (clientSecret != null && !clientSecret.isBlank()) {
                tokenParams.put("client_secret", clientSecret);
            }
            tokenParams.put("device_code", deviceCode);
            tokenParams.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            UrlEncodedContent tokenContent = new UrlEncodedContent(tokenParams);

            try {
                HttpResponse tokenHttpResp = requestFactory.buildPostRequest(tokenUrl, tokenContent).execute();

                System.out.println("token response status: " + tokenHttpResp.getStatusCode());
                System.out.println("token response content-type: " + tokenHttpResp.getContentType());

                String tokenBody;
                try {
                    tokenBody = tokenHttpResp.parseAsString();
                } catch (Exception ex) {
                    InputStream is = tokenHttpResp.getContent();
                    if (is != null) {
                        byte[] bytes = is.readAllBytes();
                        tokenBody = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    } else {
                        tokenBody = null;
                    }
                }

                if (tokenBody == null || tokenBody.isBlank()) {
                    throw new IOException("Пустой ответ от token endpoint (status=" + tokenHttpResp.getStatusCode() + ")");
                }

                com.google.api.client.json.JsonObjectParser jsonParser =
                        new com.google.api.client.json.JsonObjectParser(JSON_FACTORY);
                tokenResponse = jsonParser.parseAndClose(
                        new java.io.ByteArrayInputStream(tokenBody.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        java.nio.charset.StandardCharsets.UTF_8,
                        TokenResponse.class
                );

                break;
            } catch (HttpResponseException e) {
                String body = e.getContent();
                if (body != null && body.contains("authorization_pending")) {
                    continue;
                } else if (body != null && body.contains("slow_down")) {
                    interval += 5;
                    continue;
                } else if (body != null && body.contains("access_denied")) {
                    throw new IOException("Пользователь отклонил доступ (access_denied)");
                } else {
                    throw new IOException("Ошибка при опросе device token: " + body, e);
                }
            }
        }

        // Сохраняем credential в dataStoreFactory (flow уже создан)
        Credential credential = flow.createAndStoreCredential(tokenResponse, "user");

        // Попробуем выставить безопасные права на папку токенов
        try {
            Path p = Paths.get(tokensDir.getAbsolutePath());
            Set<java.nio.file.attribute.PosixFilePermission> perms = java.util.EnumSet.of(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                    java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
            );
            Files.setPosixFilePermissions(p, perms);
        } catch (UnsupportedOperationException | IOException ignored) { }

        System.out.println("Авторизация завершена, токены сохранены в: " + TOKENS_DIRECTORY_PATH);
        return credential;
    }


    @Override
    public String addVideoToPlaylist(String videoId) throws IOException {
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        snippet.setPlaylistId(playlistId);
        snippet.setResourceId(resourceId);

        PlaylistItem item = new PlaylistItem();
        item.setSnippet(snippet);

        YouTube.PlaylistItems.Insert request = youtube.playlistItems().insert("snippet", item);
        PlaylistItem response = request.execute();
        return response.getId();
    }

    @Override
    public void removeVideoFromPlaylist(String playlistItemId) throws IOException {
        youtube.playlistItems().delete(playlistItemId).execute();
    }

    private static String getJarDirectory() {
        try {
            String path = new File(YouTubeService.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).getParent();
            return path;
        } catch (Exception e) {
            return "."; // fallback
        }
    }

}
