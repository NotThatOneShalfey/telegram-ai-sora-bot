package com.example.tgbot.service;


import lombok.extern.slf4j.Slf4j;

import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.containers.mp4.boxes.MovieBox;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
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
 * Сервис загрузки и удаления файлов (изображения и видео) для web-интерфейса.
 * Файлы сохраняются локально и доступны по URL.
 */
@Service
@Slf4j
public class UploadService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4", "video/quicktime"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp",
            "video/mp4", "video/quicktime"
    );

    /** Длительность видео для Motion Control: 3–30 сек (общий диапазон). */
    private static final int VIDEO_MIN_SECONDS = 3;
    private static final int VIDEO_MAX_SECONDS = 30;
    /** При orientation "image": макс 10 сек. */
    private static final int VIDEO_MAX_SECONDS_ORIENTATION_IMAGE = 10;

    @Value("${web.upload.max-image-size:10MB}")
    private DataSize maxImageSize;
    @Value("${web.upload.max-video-size:100MB}")
    private DataSize maxVideoSize;

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
            log.info("Upload directory: {}", uploadPath);
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
            if (isVideoContentType(file.getContentType())) {
                validateVideoDuration(targetPath, VIDEO_MIN_SECONDS, VIDEO_MAX_SECONDS);
            }
            String url = baseUrl.replaceAll("/$", "") + endpointVersion
                    + "/v1/web/files/" + fileId;
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
     * Возвращает длительность видео в секундах по URL (только для наших загруженных файлов).
     *
     * @return длительность в секундах, или null если не удалось определить
     */
    public Double getVideoDurationSeconds(String url) {
        String fileId = extractFileId(url);
        if (fileId == null) return null;
        Path filePath = uploadPath.resolve(fileId).normalize();
        if (!filePath.startsWith(uploadPath) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return null;
        }
        try {
            return getVideoDurationSeconds(filePath);
        } catch (Exception e) {
            log.warn("Could not get video duration for {}: {}", fileId, e.getMessage());
            return null;
        }
    }

    /**
     * Проверяет длительность видео для Motion Control.
     * При orientation "image" — 3–10 сек, при "video" — 3–30 сек.
     *
     * @throws IllegalArgumentException если длительность не соответствует
     */
    public void validateVideoDurationForMotionControl(String videoUrl, String characterOrientation) {
        Double durationSec = getVideoDurationSeconds(videoUrl);
        if (durationSec == null) {
            throw new IllegalArgumentException("Не удалось определить длительность видео. Проверьте формат файла (MP4, MOV).");
        }
        int maxSec = "image".equalsIgnoreCase(characterOrientation)
                ? VIDEO_MAX_SECONDS_ORIENTATION_IMAGE
                : VIDEO_MAX_SECONDS;
        if (durationSec < VIDEO_MIN_SECONDS || durationSec > maxSec) {
            String msg = "image".equalsIgnoreCase(characterOrientation)
                    ? "При orientation «image» видео должно быть 3–10 секунд. Ваше видео — %.1f сек."
                    : "Видео должно быть 3–30 секунд. Ваше видео — %.1f сек.";
            throw new IllegalArgumentException(msg.formatted(durationSec));
        }
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

    /**
     * Удаляет загруженные файлы старше заданного количества дней.
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

    private boolean isVideoContentType(String contentType) {
        return contentType != null && VIDEO_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Недопустимый формат файла. Разрешены: изображения (JPEG, PNG, WebP), видео (MP4, MOV).");
        }
        boolean isVideo = isVideoContentType(contentType);
        DataSize maxSize = isVideo ? maxVideoSize : maxImageSize;
        long maxBytes = maxSize.toBytes();
        long fileBytes = file.getSize();
        if (fileBytes > maxBytes) {
            String typeName = isVideo ? "Видео" : "Изображение";
            String maxHuman = formatSize(maxBytes);
            String actualHuman = formatSize(fileBytes);
            throw new IllegalArgumentException(
                    "%s слишком большое. Максимальный размер — %s. Ваш файл — %s."
                            .formatted(typeName, maxHuman, actualHuman));
        }
    }

    private void validateVideoDuration(Path filePath, int minSec, int maxSec) {
        try {
            double durationSec = getVideoDurationSeconds(filePath);
            if (durationSec < minSec || durationSec > maxSec) {
                throw new IllegalArgumentException(
                        "Видео должно быть длительностью %d–%d секунд. Ваше видео — %.1f сек."
                                .formatted(minSec, maxSec, durationSec));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not validate video duration for {}: {}", filePath.getFileName(), e.getMessage());
            throw new IllegalArgumentException("Не удалось определить длительность видео. Проверьте формат файла (MP4, MOV).");
        }
    }

    private double getVideoDurationSeconds(Path filePath) throws Exception {
        SeekableByteChannel ch = null;
        try {
            ch = NIOUtils.readableFileChannel(filePath.toString());
            MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
            MovieBox movie = demuxer.getMovie();
            if (movie == null) {
                throw new IllegalStateException("No movie box");
            }
            long duration = movie.getDuration();
            int timescale = movie.getTimescale();
            return (double) duration / timescale;
        } finally {
            if (ch != null) {
                NIOUtils.closeQuietly(ch);
            }
        }
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

    private static String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return "%.0f МБ".formatted(bytes / (1024.0 * 1024));
        }
        return "%.0f КБ".formatted(bytes / 1024.0);
    }
}
