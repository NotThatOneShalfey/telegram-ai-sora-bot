package com.example.tgbot.integration.adapter;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.UserService;
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
    private final UserService userService;
    private final PriceRegistryService priceRegistryService;

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
            if (taskResponse.getCode() != 200 || taskResponse.getData() == null) {
                log.error("Kei AI generate failed: code={}, msg={}", taskResponse.getCode(), taskResponse.getMessage());
                return Optional.empty();
            }
            String taskId = taskResponse.getData().getTaskId();
            if (taskId == null || taskId.isBlank()) {
                log.error("Kei AI generate: taskId is empty");
                return Optional.empty();
            }
            session.setTaskIdForCurrentModelConfiguration(taskId, model);
            var historyId = session.getOperationsHistoryIdByTaskId(taskId);
            if (historyId != null) {
                userService.updateGenerationHistoryToProcessing(historyId, taskId);
            }
            TaskSource source = session.getRequestSource();
            sessionRegistry.putWaitingSession(taskId, session, source);
            // Обновляем баланс
            session.setUser(userService.putOnHold(session, priceRegistryService.calculatePrice(model, options, session.getUser()), options.getRequestInput()));
            // Вызываем форму, если работаем с чатом
            if (source == TaskSource.CHAT) {
                panelRegistry.getChatPanel(PanelType.SUNO_AFTER_PROMPT_RECEIVED).execute(session);
            }
            return Optional.of(taskId);
        } catch (Exception e) {
            log.error("Error sending Suno request", e);
            return Optional.empty();
        }
    }
}
