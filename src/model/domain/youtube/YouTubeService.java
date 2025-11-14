package model.domain.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.api.services.youtube.YouTubeScopes;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class YouTubeService {
    private static final String CREDENTIALS_FILE_PATH = "/client_secret_192151238148-igstb5h9vacbm5291dml39jeehkjsdqm.apps.googleusercontent.com.json"; // положите в resources
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private final YouTube youtube;
    private final String playlistId;

    public YouTubeService(String playlistId) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        this.youtube = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("AppName")
                .build();
        this.playlistId = playlistId;
    }

    private static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        InputStream in = YouTubeService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Ресурс не найден " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        List<String> scopes = Collections.singletonList(YouTubeScopes.YOUTUBE);
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));

        com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow flow =
                new com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, scopes)
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("offline")
                        .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Добавляет видео в плейлист. Возвращает id созданного PlaylistItem.
     */
    public String addVideoToPlaylist(String videoId) throws IOException {

        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        snippet.setPlaylistId(playlistId);
        snippet.setResourceId(resourceId);

        PlaylistItem item = new PlaylistItem();
        item.setSnippet(snippet);

        YouTube.PlaylistItems.Insert request = youtube.playlistItems()
                .insert("snippet", item);
        PlaylistItem response = request.execute();
        return response.getId(); // id playlistItem
    }
}
