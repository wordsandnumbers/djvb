package com.vpo.djvoxbox.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {

        try (InputStream serviceAccount = openServiceAccountStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://djvb-878ca.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }

    private InputStream openServiceAccountStream() throws IOException {
        String externalPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_PATH");
        if (externalPath == null || externalPath.isBlank()) {
            externalPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        }
        if (externalPath != null && !externalPath.isBlank()) {
            return new FileInputStream(externalPath);
        }
        return new ClassPathResource("firebaseServiceAccountKey.json").getInputStream();
    }
}
