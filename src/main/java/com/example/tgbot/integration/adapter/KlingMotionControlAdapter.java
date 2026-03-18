package com.example.tgbot.integration.adapter;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.kieai.CreateTaskResponse;
import com.example.tgbot.integration.kieai.KeiAiRequestService;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.session.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Адаптер для Kling 3.0 Motion Control API (kie.ai).
 * Вызывается только из web-интерфейса — перенос движения из референсного видео на изображение персонажа.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KlingMotionControlAdapter implements IRequestAdapter {
    @Getter
    private final GenerationModel model = GenerationModel.KLING_3_MOTION_CONTROL;
    private final KeiAiRequestService requestService;
    @Value("${telegram.bot.version-endpoint:}")
    private String endpointVersion;
    @Value("${telegram.bot.webhook-base-url:}")
    private String baseUrl;
    private final SessionRegistry sessionRegistry;
    private final UserService userService;
    private final PriceRegistryService priceRegistryService;

    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public Optional<String> makeRequest(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(model);
        if (options == null) {
            log.error("KlingMotionControl: no options for model");
            return Optional.empty();
        }
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/kling-3-motion-control";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model.getRequestModelName());
        payload.put("callBackUrl", fullCallbackUrl);
        payload.put("input", options.getRequestInput());
        try {
            String response = requestService.sendPostRequest("/jobs/createTask", mapper.writeValueAsString(payload));
            log.trace("KlingMotionControl response: {}", response);
            CreateTaskResponse taskResponse = mapper.readValue(response, CreateTaskResponse.class);
            if (taskResponse.getCode() != 200 || taskResponse.getData() == null) {
                log.error("Kei AI createTask (Kling Motion Control) failed: code={}, msg={}",
                        taskResponse.getCode(), taskResponse.getMessage());
                return Optional.empty();
            }
            String taskId = taskResponse.getData().getTaskId();
            if (taskId == null || taskId.isBlank()) {
                log.error("Kei AI createTask (Kling Motion Control): taskId is empty");
                return Optional.empty();
            }
            session.setTaskIdForCurrentModelConfiguration(taskId, model);
            var historyId = session.getOperationsHistoryIdByTaskId(taskId);
            if (historyId != null) {
                userService.updateGenerationHistoryToProcessing(historyId, taskId);
            }
            sessionRegistry.putWaitingSession(taskId, session, TaskSource.WEB);
            session.setUser(userService.putOnHold(session, priceRegistryService.calculatePrice(model, options, session.getUser()), options.getRequestInput()));
            return Optional.of(taskId);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Error during Kling Motion Control request: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error sending Kling Motion Control request", e);
            return Optional.empty();
        }
    }
}
