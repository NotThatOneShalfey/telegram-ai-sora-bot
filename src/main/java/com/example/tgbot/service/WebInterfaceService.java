package com.example.tgbot.service;

import com.example.tgbot.models.configurations.dto.InterfaceDTORequest;
import com.example.tgbot.models.configurations.dto.WebGenerateResponse;
import com.example.tgbot.models.configurations.dto.WebSubmitResult;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.registry.TaskResultRegistry;
import com.example.tgbot.telegram.TgBot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Прослойка между HTTP-контроллером и TgBot.
 * Принимает запросы веб-интерфейса и передаёт их в TgBot для асинхронной обработки.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebInterfaceService {
    private final TgBot tgBot;
    private final TaskResultRegistry taskResultRegistry;
    private final ObjectMapper objectMapper;

    public void submitGenerationRequest(InterfaceDTORequest request) {
        log.trace("WebInterfaceService: submitting request for model={}, userId={}", request.getModel(), request.getUserId());
        tgBot.onWebInterfaceRequest(request);
    }

    /** Синхронно отправляет задачу и возвращает taskId и баланс при успехе. */
    public Optional<WebSubmitResult> submitAndGetTaskId(InterfaceDTORequest request) {
        log.trace("WebInterfaceService: submitting request for model={}, userId={}", request.getModel(), request.getUserId());
        return tgBot.processWebInterfaceRequestSync(request);
    }

    /**
     * Возвращает результат выполненной задачи по userId (User.telegramId) и taskId.
     * Результат содержит опции модели и результирующие ссылки.
     * Результат извлекается из реестра и удаляется после чтения.
     */
    public Optional<WebGenerateResponse<?>> getTaskResult(Long userId, String taskId) {
        TaskResultRegistry.TaskResultRecord record = taskResultRegistry.get(taskId);
        if (record == null) {
            return Optional.empty();
        }
        if (!record.getUserId().equals(userId)) {
            return Optional.empty();
        }
        try {
            Object options = parseOptions(record.getModel(), record.getOptionsJson());
            WebGenerateResponse<Object> response = new WebGenerateResponse<>(record.getLinks(), options);
            taskResultRegistry.remove(taskId);
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse options for taskId={}, model={}", taskId, record.getModel(), e);
            return Optional.empty();
        }
    }

    private Object parseOptions(GenerationModel model, String optionsJson) throws JsonProcessingException {
        return switch (model) {
            case KLING_3_0 -> objectMapper.readValue(optionsJson, KlingOptionsDTO.class);
            case SORA_2, SORA_2_WITH_IMAGE -> objectMapper.readValue(optionsJson, SoraOptionsDTO.class);
            case SUNO_V5 -> objectMapper.readValue(optionsJson, SunoOptionsDTO.class);
            case NANO_BANANA_PRO -> objectMapper.readValue(optionsJson, NanoBananaOptionsDTO.class);
        };
    }
}
