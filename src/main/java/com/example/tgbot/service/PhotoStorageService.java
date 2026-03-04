package com.example.tgbot.service;

import com.example.tgbot.models.data.PhotoUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PhotoStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadDir;
    private final String uploadPathPrefix;

    public PhotoStorageService(
            @Value("${app.uploads.directory:uploads/photos}") String uploadDirectory,
            @Value("${app.uploads.path-prefix:/uploads/photos}") String pathPrefix) {
        this.uploadDir = Path.of(uploadDirectory).toAbsolutePath();
        this.uploadPathPrefix = pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/";
        ensureUploadDirExists();
    }

    private void ensureUploadDirExists() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + uploadDir, e);
        }
    }

    public PhotoUploadResponse storePhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid content type. Allowed: " + ALLOWED_CONTENT_TYPES);
        }

        String extension = getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: " + ALLOWED_EXTENSIONS);
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path targetPath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath);

        String urlPath = uploadPathPrefix + filename;
        log.debug("Photo stored: {}", filename);

        return PhotoUploadResponse.builder()
                .filename(filename)
                .url(urlPath)
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
