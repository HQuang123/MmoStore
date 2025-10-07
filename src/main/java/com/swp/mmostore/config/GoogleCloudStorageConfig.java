package com.swp.mmostore.config;

import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GoogleCloudStorageConfig {
    @Value("${GCP_CREDENTIALS}")
    private String credentialsJson;

    @Bean
    public Storage storage() throws IOException {
        // Parse JSON from env var
        GoogleCredentials credentials;
        try (InputStream credentialsStream =
                     new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
            credentials = GoogleCredentials.fromStream(credentialsStream);
        }

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
