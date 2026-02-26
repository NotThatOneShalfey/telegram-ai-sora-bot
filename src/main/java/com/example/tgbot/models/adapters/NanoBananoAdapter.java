package com.example.tgbot.models.adapters;

import com.example.tgbot.models.KeiAiRequestService;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.configurations.NanoBananaOptions;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NanoBananoAdapter implements IRequestAdapter {
    @Getter
    private final GenerationModel model = GenerationModel.NANO_BANANA_PRO;
    private final KeiAiRequestService requestService;
    @Value("${telegram.bot.version-endpoint:}")
    private String endpointVersion;
    @Value("${telegram.bot.webhook-base-url:}")
    private String baseUrl;

    ObjectMapper mapper = new JsonMapper();

    @Override
    public void makeRequest(UserSession session) {
        ModelRequestOptions options = session.getModelsConfiguration().get(model);
        String fullCallbackUrl = baseUrl + endpointVersion + "/callbacks/nano-banana-pro";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model.getRequestModelName());
        payload.put("callBackUrl", fullCallbackUrl);
        payload.put("input", options.getRequestInput());
        try {
            String response = requestService.sendPostRequest("/jobs/createTask", mapper.writeValueAsString(payload));
            System.out.println("Ответ: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
