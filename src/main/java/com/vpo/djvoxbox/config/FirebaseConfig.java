package com.vpo.djvoxbox.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {

        Resource resource = new ClassPathResource("firebaseServiceAccountKey.json");
        InputStream serviceAccount = resource.getInputStream();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://djvb-878ca.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

    }
}