package com.example.tgbot.controllers;

import com.example.tgbot.models.configurations.dto.*;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.ImageUploadService;
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
    public ResponseEntity<Resource> getFile(@PathVariable String fileId) {
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
     * Получение результата задачи по userId (User.telegramId) и taskId.
     * Возвращает WebGenerateResponse с опциями модели и результирующими ссылками.
     */
    @GetMapping("/result")
    public ResponseEntity<?> getTaskResult(
            @RequestParam Long userId,
            @RequestParam String taskId) {
        Optional<WebGenerateResponse<?>> result = webInterfaceService.getTaskResult(userId, taskId);
        return result
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private <T> ResponseEntity<?> processGenerate(String body, GenerationModel model, TypeReference<WebGenerateRequest<T>> typeRef) {
        try {
            WebGenerateRequest<T> request = mapper.readValue(body, typeRef);
            String optionsBody = mapper.writeValueAsString(request.getOptions());
            InterfaceDTORequest dtoRequest = new InterfaceDTORequest(model, request.getUserId(), optionsBody);
            Optional<String> taskId = webInterfaceService.submitAndGetTaskId(dtoRequest);
            return taskId
                    .map(id -> ResponseEntity.ok(new WebGenerateSubmittedResponse(id)))
                    .orElse(ResponseEntity.internalServerError().build());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
