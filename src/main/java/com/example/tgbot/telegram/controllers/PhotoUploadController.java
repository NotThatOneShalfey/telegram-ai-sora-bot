package com.example.tgbot.telegram.controllers;

import com.example.tgbot.models.data.PhotoUploadResponse;
import com.example.tgbot.service.PhotoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * API controller for frontend (React Mini App).
 * Accepts photo upload via multipart/form-data and returns the URL to access the stored photo.
 */
@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoUploadController {

    private final PhotoStorageService photoStorageService;

    @Value("${app.base-url:}")
    private String baseUrl;

    /**
     * Upload a photo from the device.
     * Frontend should send multipart/form-data with key "photo" or "file".
     *
     * Example (React):
     * const formData = new FormData();
     * formData.append('photo', file);
     * fetch('/api/v1/photos/upload', { method: 'POST', body: formData })
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponse> uploadPhoto(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        MultipartFile upload = photo != null && !photo.isEmpty() ? photo : file;
        if (upload == null || upload.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PhotoUploadResponse response = photoStorageService.storePhoto(upload);
            String url = response.getUrl();
            if (baseUrl != null && !baseUrl.isBlank()) {
                String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                url = base + (url.startsWith("/") ? url : "/" + url);
                response = PhotoUploadResponse.builder()
                        .url(url)
                        .filename(response.getFilename())
                        .build();
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Failed to store photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
