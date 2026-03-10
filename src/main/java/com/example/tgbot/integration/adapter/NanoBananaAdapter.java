package com.example.tgbot.integration.adapter;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.kieai.CreateTaskResponse;
import com.example.tgbot.integration.kieai.KeiAiRequestService;
import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NanoBananaAdapter implements IRequestAdapter {
    @Getter
    private final GenerationModel model = GenerationModel.NANO_BANANA_PRO;
    private final KeiAiRequestService requestService;
    @Value("${telegram.bot.version-endpoint:}")
    private String endpointVersion;
    @Value("${telegram.bot.webhook-base-url:}")
    private String baseUrl;
    @Lazy
    private final PanelRegistry panelRegistry;
    private final SessionRegistry sessionRegistry;

    ObjectMapper mapper = new JsonMapper();

    @Override
    public Optional<String> makeRequest(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(model);
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/nano-banana-pro";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model.getRequestModelName());
        payload.put("callBackUrl", fullCallbackUrl);
        payload.put("input", options.getRequestInput());
        try {
            String response = requestService.sendPostRequest("/jobs/createTask", mapper.writeValueAsString(payload));
            log.trace("Response: {}", response);
            CreateTaskResponse taskResponse = mapper.readValue(response, CreateTaskResponse.class);
            if (taskResponse.getCode() != 200 || taskResponse.getData() == null) {
                log.error("Kei AI createTask failed: code={}, msg={}", taskResponse.getCode(), taskResponse.getMessage());
                return Optional.empty();
            }
            String taskId = taskResponse.getData().getTaskId();
            if (taskId == null || taskId.isBlank()) {
                log.error("Kei AI createTask: taskId is empty");
                return Optional.empty();
            }
            session.setTaskIdForCurrentModelConfiguration(taskId, model);
            TaskSource source = session.getRequestSource();
            sessionRegistry.putWaitingSession(taskId, session, source);
            if (source == TaskSource.CHAT) {
                panelRegistry.getChatPanel(PanelType.NANO_BANANA_AFTER_PROMPT_RECEIVED).execute(session);
            }
            return Optional.of(taskId);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Error during mapping response onto CreateTaskResponse Object -> {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error sending NanoBanana request", e);
            return Optional.empty();
        }
    }
}
