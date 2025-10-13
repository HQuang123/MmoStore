package com.swp.mmostore.service;

import com.google.cloud.storage.Blob;
import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
@Service
public class CloudStorageService {

    @Value("${gcp.bucket}")
    private String bucketName;

    private final Storage storage;

    public CloudStorageService(Storage storage) {
        this.storage = storage;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String blobName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, blobName);
    }

    // Lấy file dưới dạng byte[]
    public byte[] downloadFile(String blobName) throws IOException {
        Blob blob = storage.get(bucketName, blobName);
        if (blob == null) {
            throw new IOException("Không tìm thấy file trong GCS: " + blobName);
        }
        return blob.getContent();
    }


}
