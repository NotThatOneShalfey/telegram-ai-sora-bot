package com.example.tgbot.models.adapters;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.KeiAiRequestService;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.configurations.SoraOptions;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
    private final ObjectProvider<RegistryService> registryServiceProvider;

    ObjectMapper mapper = new JsonMapper();

    @Override
    public void makeRequest(UserSession session) {
        ModelRequestOptions options = session.getCurrentRequestOptionsByModel(model);
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/sora2";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", options.getModel().getRequestModelName());
        payload.put("callBackUrl", fullCallbackUrl);
        payload.put("input", options.getRequestInput());
        try {
            String response = requestService.sendPostRequest("/jobs/createTask", mapper.writeValueAsString(payload));
            log.trace("Response: {}", response);
            try {
                CreateTaskResponse taskResponse = mapper.readValue(response, CreateTaskResponse.class);
                String taskId = taskResponse.getData().getTaskId();
                session.setTaskIdForCurrentModelConfiguration(taskId, model);
                registryServiceProvider.getObject().putWaitingSession(taskId, session);
                registryServiceProvider.getObject().getChatPanel(PanelType.SORA_2_AFTER_PROMPT_RECEIVED).execute(session);
            } catch (JsonProcessingException | RuntimeException e) {
                log.error("Error during mapping response onto CreateTaskResponse Object -> {}", e.toString());
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
