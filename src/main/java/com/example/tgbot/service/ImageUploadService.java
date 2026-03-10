package com.example.tgbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис загрузки и удаления изображений для web-интерфейса.
 * Файлы сохраняются локально и доступны по URL.
 */
@Service
@Slf4j
public class ImageUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 10 MB

    @Value("${web.uploaded-files-dir:./uploaded-files}")
    private String uploadDir;

    @Value("${web.uploaded-files-base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${telegram.bot.version-endpoint:}")
    private String endpointVersion;

    private Path uploadPath;

    @PostConstruct
    void init() {
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            log.info("Image upload directory: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Сохраняет загруженные файлы и возвращает их публичные URL.
     */
    public List<String> saveFiles(MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            validateFile(file);
            String fileId = UUID.randomUUID().toString();
            Path targetPath = uploadPath.resolve(fileId);
            file.transferTo(targetPath.toFile());
            String url = baseUrl.replaceAll("/$", "") + endpointVersion
                    + "/v1/web/files/" + fileId + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
            urls.add(url);
            log.trace("Saved uploaded file: {} -> {}", fileId, url);
        }
        return urls;
    }

    /**
     * Проверяет, принадлежит ли URL нашим загруженным файлам.
     */
    public boolean isOurUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String prefix = baseUrl.replaceAll("/$", "") + endpointVersion + "/v1/web/files/";
        return url.startsWith(prefix);
    }

    /**
     * Извлекает fileId из URL, если это наш URL.
     */
    public String extractFileId(String url) {
        if (!isOurUrl(url)) return null;
        String prefix = baseUrl.replaceAll("/$", "") + endpointVersion + "/v1/web/files/";
        return url.substring(prefix.length()).split("[?#]")[0].trim();
    }

    /**
     * Удаляет файлы по списку URL (только наши загруженные).
     */
    public void deleteByUrls(List<String> urls) {
        for (String url : urls) {
            String fileId = extractFileId(url);
            if (fileId != null) {
                deleteByFileId(fileId);
            }
        }
    }

    /**
     * Возвращает файл как Resource для раздачи.
     */
    public Resource getFileAsResource(String fileId) throws MalformedURLException {
        Path filePath = uploadPath.resolve(fileId).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new SecurityException("Invalid file path");
        }
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return null;
        }
        return new UrlResource(filePath.toUri());
    }

    private void deleteByFileId(String fileId) {
        try {
            Path filePath = uploadPath.resolve(fileId).normalize();
            if (filePath.startsWith(uploadPath) && Files.exists(filePath) && Files.isRegularFile(filePath)) {
                Files.delete(filePath);
                log.trace("Deleted uploaded file: {}", fileId);
            }
        } catch (IOException e) {
            log.warn("Failed to delete uploaded file {}: {}", fileId, e.getMessage());
        }
    }

    /**
     * Удаляет загруженные файлы старше заданного количества дней.
     * Использует время последней модификации файла (при сохранении оно равно времени создания).
     */
    public int deleteFilesOlderThanDays(int days) {
        if (days <= 0) return 0;
        Instant cutoff = Instant.now().minus(Duration.ofDays(days));
        int deleted = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadPath)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) continue;
                BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                Instant fileTime = attrs.lastModifiedTime().toInstant();
                if (fileTime.isBefore(cutoff)) {
                    Files.delete(entry);
                    deleted++;
                    log.trace("Deleted old uploaded file: {}", entry.getFileName());
                }
            }
            if (deleted > 0) {
                log.info("Deleted {} old uploaded files (older than {} days)", deleted, days);
            }
        } catch (IOException e) {
            log.warn("Failed to clean old uploaded files: {}", e.getMessage());
        }
        return deleted;
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid content type: " + contentType + ". Allowed: jpeg, png, webp");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File too large. Max size: 10 MB");
        }
    }
}
