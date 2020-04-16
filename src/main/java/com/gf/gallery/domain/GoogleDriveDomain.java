package com.gf.gallery.domain;

import com.gf.gallery.entities.GalleryImage;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveDomain {
    private static final String APPLICATION_NAME = "GF Gallery";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "../../../../credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveDomain.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public List<GalleryImage> getImages() throws Exception {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String pageToken = null;

        FileList filteredFiles = service.files().list().setQ("parents = '1ZuoSTompnG5o1pe4JEKqPSHsvJz689dP' and mimeType='image/jpeg'")
                .setFields("nextPageToken, files(id, name, imageMediaMetadata)")
                .setPageToken(pageToken)
                .setPageSize(300)
                .execute();

        List<File> files = filteredFiles.getFiles();



        if (files == null || files.isEmpty()) {
            throw new Exception("No files found");
        }

        List<GalleryImage> galleryImages = new ArrayList<GalleryImage>();
        int count = 0;
            for (File file : files) {
                GalleryImage galleryImage = new GalleryImage();
                galleryImage.setSrc("https://drive.google.com/uc?id="+file.getId());
                File.ImageMediaMetadata imageMediaMetadata = file.getImageMediaMetadata();
                galleryImage.setWidth(imageMediaMetadata.getWidth());
                galleryImage.setHeight(imageMediaMetadata.getHeight());
                galleryImage.setIndex(++count);

                galleryImages.add(galleryImage);
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
            return galleryImages;
    }
}