package com.example.tgbot.models.adapters;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.KeiAiRequestService;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.data.CreateTaskResponse;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    private final ObjectProvider<RegistryService> registryServiceProvider;

    ObjectMapper mapper = new JsonMapper();

    @Override
    public void makeRequest(UserSession session) {
        IModelRequestOptions options = session.getCurrentRequestOptionsByModel(model);
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/suno-v5";
        Map<String, Object> payload = options.getRequestInput();
        payload.put("callBackUrl", fullCallbackUrl);
        try {
            String response = requestService.sendPostRequest("/generate", mapper.writeValueAsString(payload));
            CreateTaskResponse taskResponse  = mapper.readValue(response, CreateTaskResponse.class);
            String taskId = taskResponse.getData().getTaskId();
            session.setTaskIdForCurrentModelConfiguration(taskId, model);
            registryServiceProvider.getObject().putWaitingSession(taskId, session);
            registryServiceProvider.getObject().getChatPanel(PanelType.SUNO_AFTER_PROMPT_RECEIVED).execute(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
