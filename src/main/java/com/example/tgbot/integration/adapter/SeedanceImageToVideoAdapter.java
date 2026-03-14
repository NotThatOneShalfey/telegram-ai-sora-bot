package com.example.tgbot.integration.adapter;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.config.SeedanceImageToVideoOptions;
import com.example.tgbot.integration.seedance.SeedanceCreateTaskResponse;
import com.example.tgbot.integration.seedance.SeedanceRequestService;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.SeedancePollingRegistry;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.session.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Адаптер для Seedance 2.0 API (text-to-video и image-to-video).
 * Только web-интерфейс. Результат получается через polling, не callback.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeedanceImageToVideoAdapter implements IRequestAdapter {
    @Override
    public GenerationModel getModel() {
        return GenerationModel.SEEDANCE_2_0;
    }

    private final SeedanceRequestService requestService;
    private final SessionRegistry sessionRegistry;
    private final SeedancePollingRegistry pollingRegistry;
    private final UserService userService;
    private final PriceRegistryService priceRegistryService;

    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public java.util.Optional<String> makeRequest(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(getModel());
        if (options == null || !(options instanceof SeedanceImageToVideoOptions seedanceOptions)) {
            log.error("Seedance: no options for model");
            return java.util.Optional.empty();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "seedance-20");
        payload.put("inputs", seedanceOptions.getRequestInput());
        try {
            String response = requestService.createTask(mapper.writeValueAsString(payload));
            SeedanceCreateTaskResponse taskResponse = mapper.readValue(response, SeedanceCreateTaskResponse.class);
            String taskId = taskResponse.getTaskId();
            if (taskId == null || taskId.isBlank()) {
                log.error("Seedance createTask: taskId is empty");
                return java.util.Optional.empty();
            }
            session.setTaskIdForCurrentModelConfiguration(taskId, getModel());
            var historyId = session.getOperationsHistoryIdByTaskId(taskId);
            if (historyId != null) {
                userService.updateGenerationHistoryToProcessing(historyId, taskId);
            }
            sessionRegistry.putWaitingSession(taskId, session, TaskSource.WEB);
            session.setUser(userService.putOnHold(session, priceRegistryService.calculatePrice(getModel(), options, session.getUser()), options.getRequestInput()));
            pollingRegistry.register(taskId);
            return java.util.Optional.of(taskId);
        } catch (Exception e) {
            log.error("Error during Seedance request: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }
}
