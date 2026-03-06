package com.example.tgbot.integration.adapter;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.kieai.CreateTaskResponse;
import com.example.tgbot.integration.kieai.KeiAiRequestService;
import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SunoAdapter implements IRequestAdapter {
    @Getter
    private final GenerationModel model = GenerationModel.SUNO_V5;
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
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/suno-v5";
        Map<String, Object> payload = options.getRequestInput();
        payload.put("callBackUrl", fullCallbackUrl);
        try {
            String response = requestService.sendPostRequest("/generate", mapper.writeValueAsString(payload));
            CreateTaskResponse taskResponse = mapper.readValue(response, CreateTaskResponse.class);
            String taskId = taskResponse.getData().getTaskId();
            session.setTaskIdForCurrentModelConfiguration(taskId, model);
            sessionRegistry.putWaitingSession(taskId, session);
            panelRegistry.getChatPanel(PanelType.SUNO_AFTER_PROMPT_RECEIVED).execute(session);
            return Optional.of(taskId);
        } catch (Exception e) {
            log.error("Error sending Suno request", e);
            return Optional.empty();
        }
    }
}
