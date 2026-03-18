package com.example.tgbot.service;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.*;
import com.example.tgbot.registry.TaskErrorRegistry;
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
    private final TaskErrorRegistry taskErrorRegistry;
    private final ObjectMapper objectMapper;

    public void submitGenerationRequest(InterfaceDTORequest request) {
        log.trace("WebInterfaceService: submitting request for model={}, userId={}", request.getModel(), request.getUserId());
        tgBot.onWebInterfaceRequest(request);
    }

    /** Синхронно отправляет задачу. При успехе — SubmitOutcome.ok, при ошибке — SubmitOutcome.fail с ErrorCode. */
    public SubmitOutcome submitAndGetTaskId(InterfaceDTORequest request) {
        log.trace("WebInterfaceService: submitting request for model={}, userId={}", request.getModel(), request.getUserId());
        return tgBot.processWebInterfaceRequestSync(request);
    }

    /**
     * Возвращает результат выполненной задачи по userId и taskId.
     * При успехе — WebGenerateResponse. При ошибке задачи — ErrorResponseDTO.
     * При отсутствии — Optional.empty().
     */
    public Optional<Object> getTaskResult(Long userId, String taskId) {
        TaskResultRegistry.TaskResultRecord record = taskResultRegistry.get(taskId);
        if (record != null && record.getUserId().equals(userId)) {
            try {
                Object options = parseOptions(record.getModel(), record.getOptionsJson());
                WebGenerateResponse<Object> response = new WebGenerateResponse<>(
                        record.getResultItems(),
                        record.getModel(),
                        record.getBalanceChange(),
                        options
                );
                taskResultRegistry.remove(taskId);
                return Optional.of(response);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse options for taskId={}, model={}", taskId, record.getModel(), e);
            }
        }
        TaskErrorRegistry.TaskErrorRecord errorRecord = taskErrorRegistry.get(taskId);
        if (errorRecord != null && errorRecord.getUserId().equals(userId)) {
            taskErrorRegistry.remove(taskId);
            return Optional.of(ErrorResponseDTO.from(errorRecord.getErrorCode()));
        }
        return Optional.empty();
    }

    private Object parseOptions(GenerationModel model, String optionsJson) throws JsonProcessingException {
        return switch (model) {
            case KLING_3_0 -> objectMapper.readValue(optionsJson, KlingOptionsDTO.class);
            case KLING_3_MOTION_CONTROL -> objectMapper.readValue(optionsJson, KlingMotionControlOptionsDTO.class);
            case SEEDANCE_2_0 -> objectMapper.readValue(optionsJson, SeedanceImageToVideoOptionsDTO.class);
            case ELEVENLABS_V3 -> objectMapper.readValue(optionsJson, ElevenLabsOptionsDTO.class);
            case SORA_2, SORA_2_WITH_IMAGE -> objectMapper.readValue(optionsJson, SoraOptionsDTO.class);
            case SUNO_V5 -> objectMapper.readValue(optionsJson, SunoOptionsDTO.class);
            case NANO_BANANA_PRO -> objectMapper.readValue(optionsJson, NanoBananaOptionsDTO.class);
        };
    }
}
