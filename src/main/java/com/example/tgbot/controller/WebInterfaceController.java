package com.example.tgbot.controller;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.enums.GenerationType;
import com.example.tgbot.dto.api.*;
import com.example.tgbot.service.ImageUploadService;
import com.example.tgbot.service.OperationsHistoryService;
import com.example.tgbot.service.UserQueryService;
import com.example.tgbot.service.WebInterfaceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/web")
@RequiredArgsConstructor
@Slf4j
public class WebInterfaceController {
    private final WebInterfaceService webInterfaceService;
    private final ImageUploadService imageUploadService;
    private final OperationsHistoryService operationsHistoryService;
    private final UserQueryService userQueryService;
    private final ObjectMapper mapper = new JsonMapper();

    @PostMapping("/kling")
    public ResponseEntity<?> generateKling(@RequestBody String body) {
        return processGenerate(body, GenerationModel.KLING_3_0, new TypeReference<WebGenerateRequest<KlingOptionsDTO>>() {});
    }

    @PostMapping("/sora2")
    public ResponseEntity<?> generateSora(@RequestBody String body) {
        return processGenerate(body, GenerationModel.SORA_2, new TypeReference<WebGenerateRequest<SoraOptionsDTO>>() {});
    }

    @PostMapping("/suno")
    public ResponseEntity<?> generateSuno(@RequestBody String body) {
        return processGenerate(body, GenerationModel.SUNO_V5, new TypeReference<WebGenerateRequest<SunoOptionsDTO>>() {});
    }

    @PostMapping("/nanobanana")
    public ResponseEntity<?> generateNanoBanana(@RequestBody String body) {
        return processGenerate(body, GenerationModel.NANO_BANANA_PRO, new TypeReference<WebGenerateRequest<NanoBananaOptionsDTO>>() {});
    }

    /**
     * Загрузка изображений. Принимает multipart/form-data с частью "images".
     * Возвращает {"urls": ["https://...", ...]} — эти URL передавать в imageUrls/imageInput при генерации.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@RequestParam("images") MultipartFile[] images) {
        try {
            List<String> urls = imageUploadService.saveFiles(images);
            Map<String, List<String>> response = new HashMap<>();
            response.put("urls", urls);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to upload images", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Раздача загруженных файлов по fileId. URL формируется как base-url/v1/web/files/{fileId}.
     */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> getFile(@PathVariable(name = "fileId") String fileId) {
        try {
            Resource resource = imageUploadService.getFileAsResource(fileId);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (Exception e) {
            log.warn("Failed to serve file {}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение результата задачи по userId и taskId (polling).
     * При успехе — WebGenerateResponse. При ошибке задачи — ErrorResponseDTO (code, description).
     */
    @GetMapping("/result")
    public ResponseEntity<?> getTaskResult(
            @RequestParam("userId") String userId,
            @RequestParam("taskId") String taskId) {
        try {
            Long parsedUserId = parseUserId(userId);
            Optional<Object> result = webInterfaceService.getTaskResult(parsedUserId, taskId);
            return result
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * История генераций видео (Sora, Kling). Список отсортирован по дате по убыванию.
     */
    @GetMapping("/history/video")
    public ResponseEntity<?> getVideoHistory(@RequestParam("userId") String userId) {
        return getHistoryByType(userId, GenerationType.VIDEO);
    }

    /**
     * История генераций музыки (Suno). Список отсортирован по дате по убыванию.
     */
    @GetMapping("/history/music")
    public ResponseEntity<?> getMusicHistory(@RequestParam("userId") String userId) {
        return getHistoryByType(userId, GenerationType.MUSIC);
    }

    /**
     * История генераций изображений (Nano Banana Pro). Список отсортирован по дате по убыванию.
     */
    @GetMapping("/history/image")
    public ResponseEntity<?> getImageHistory(@RequestParam("userId") String userId) {
        return getHistoryByType(userId, GenerationType.IMAGE);
    }

    /**
     * Информация о пользователе по userId (telegram ID): баланс и статус амбассадора.
     * Ответ: {"balance": int, "ambassador": boolean}
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(@RequestParam("userId") String userId) {
        try {
            Long parsedUserId = parseUserId(userId);
            return userQueryService.findUser(parsedUserId)
                    .map(user -> {
                        Map<String, Object> body = new HashMap<>();
                        body.put("balance", user.getBalance() != null ? user.getBalance() : 0);
                        body.put("ambassador", user.isAmbassador());
                        return ResponseEntity.ok(body);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ResponseEntity<?> getHistoryByType(String userId, GenerationType type) {
        try {
            Long parsedUserId = parseUserId(userId);
            return ResponseEntity.ok(operationsHistoryService.getHistory(parsedUserId, type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        try {
            return Long.parseLong(userId.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("userId must be a valid number");
        }
    }

    private <T> ResponseEntity<?> processGenerate(String body, GenerationModel model, TypeReference<WebGenerateRequest<T>> typeRef) {
        try {
            WebGenerateRequest<T> request = mapper.readValue(body, typeRef);
            Long userId = parseUserId(request.getUserId());
            String optionsBody = mapper.writeValueAsString(request.getOptions());
            InterfaceDTORequest dtoRequest = new InterfaceDTORequest(model, userId, optionsBody);
            Optional<WebSubmitResult> result = webInterfaceService.submitAndGetTaskId(dtoRequest);
            return result
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.internalServerError().build());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
