package com.donttaz.donttazapi.infra.push.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class FirebaseAdminConfig {

    private static final String FIREBASE_APP_NAME = "donttaz-firebase";

    @Bean
    @ConditionalOnExpression("'${firebase.service-account-path:}' != ''")
    public FirebaseApp firebaseApp(
            @Value("${firebase.service-account-path}") String serviceAccountPath,
            ResourceLoader resourceLoader
    ) throws IOException {
        Optional<FirebaseApp> existingApp = FirebaseApp.getApps().stream()
                .filter(app -> FIREBASE_APP_NAME.equals(app.getName()))
                .findFirst();

        if (existingApp.isPresent()) {
            return existingApp.get();
        }

        Resource resource = resourceLoader.getResource(resolveResourceLocation(serviceAccountPath));
        try (InputStream inputStream = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();
            return FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
        }
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private String resolveResourceLocation(String serviceAccountPath) {
        if (serviceAccountPath.startsWith("classpath:") || serviceAccountPath.startsWith("file:")) {
            return serviceAccountPath;
        }

        return Path.of(serviceAccountPath).toUri().toString();
    }
}
