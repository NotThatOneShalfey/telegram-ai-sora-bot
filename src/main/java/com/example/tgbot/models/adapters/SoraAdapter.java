package com.example.tgbot.models.adapters;

import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.models.KeiAiRequestService;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.data.CreateTaskResponse;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
public class SoraAdapter implements IRequestAdapter {

    @Getter
    @Setter
    private GenerationModel model = GenerationModel.SORA_2;
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
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/sora2";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", options.getModel().getRequestModelName());
        payload.put("callBackUrl", fullCallbackUrl);
        payload.put("input", options.getRequestInput());
        try {
            String response = requestService.sendPostRequest("/jobs/createTask", mapper.writeValueAsString(payload));
            log.trace("Response: {}", response);
            CreateTaskResponse taskResponse = mapper.readValue(response, CreateTaskResponse.class);
            String taskId = taskResponse.getData().getTaskId();
            session.setTaskIdForCurrentModelConfiguration(taskId, model);
            sessionRegistry.putWaitingSession(taskId, session);
            panelRegistry.getChatPanel(PanelType.SORA_2_AFTER_PROMPT_RECEIVED).execute(session);
            return Optional.of(taskId);
        } catch (Exception e) {
            log.error("Error sending Sora request", e);
            return Optional.empty();
        }
    }

}
