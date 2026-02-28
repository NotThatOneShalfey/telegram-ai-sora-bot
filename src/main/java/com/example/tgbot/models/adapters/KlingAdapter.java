package com.example.tgbot.models.adapters;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.KeiAiRequestService;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.data.CreateTaskResponse;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KlingAdapter implements IRequestAdapter {
    @Getter
    private final GenerationModel model = GenerationModel.KLING_3_0;
    private final KeiAiRequestService requestService;
    @Value("${telegram.bot.version-endpoint:}")
    private String endpointVersion;
    @Value("${telegram.bot.webhook-base-url:}")
    private String baseUrl;
    private final ObjectProvider<RegistryService> registryServiceProvider;

    private final ObjectMapper mapper = new JsonMapper();

    @Override
    public void makeRequest(UserSession session) {
        ModelRequestOptions options = session.getCurrentRequestOptionsByModel(GenerationModel.KLING_3_0);
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/kling-3-0";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model.getRequestModelName());
        payload.put("callBackUrl", fullCallbackUrl);
        payload.put("input", options.getRequestInput());
        try {
            String response = requestService.sendPostRequest("/jobs/createTask", mapper.writeValueAsString(payload));
            log.trace("Response: {}", response);
            try {
                CreateTaskResponse taskResponse = mapper.readValue(response, CreateTaskResponse.class);
                String taskId = taskResponse.getData().getTaskId();
                session.setTaskIdForCurrentModelConfiguration(taskId, GenerationModel.KLING_3_0);
                registryServiceProvider.getObject().putWaitingSession(taskId, session);
            } catch (JsonProcessingException | RuntimeException e) {
                log.error("Error during mapping response onto CreateTaskResponse Object -> {}", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
