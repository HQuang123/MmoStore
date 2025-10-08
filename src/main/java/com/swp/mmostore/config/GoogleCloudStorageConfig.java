package com.swp.mmostore.config;

import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GoogleCloudStorageConfig {
    @Value("${GCP_CREDENTIALS:}")
    private String credentialsJson;

    @Value("${GCP_CREDENTIALS_FILE:}")
    private String credentialsFile;

    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credentials;

        // 1️⃣ If running on GCP VM, use default credentials (auto service account)
        try {
            credentials = GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            // 2️⃣ Fallback to developer-defined credentials
            credentials = loadLocalCredentials();
        }

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    private GoogleCredentials loadLocalCredentials() throws IOException {
        // Try file first
        if (credentialsFile != null && !credentialsFile.isEmpty()) {
            try (InputStream in = new FileInputStream(credentialsFile)) {
                return GoogleCredentials.fromStream(in);
            }
        }

        // Fallback to JSON from environment variable
        if (credentialsJson != null && !credentialsJson.isEmpty()) {
            try (InputStream in =
                         new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(in);
            }
        }

        throw new IOException("No GCP credentials found (GCP_CREDENTIALS or GCP_CREDENTIALS_FILE not set)");
    }
}
