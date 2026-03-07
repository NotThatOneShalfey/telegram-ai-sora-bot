package com.example.tgbot.telegram.handler;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.Operation;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.kieai.ReceivedFile;
import com.example.tgbot.integration.kieai.RecordInfoResponse;
import com.example.tgbot.integration.kieai.SunoInfoResponse;
import com.example.tgbot.registry.ButtonRegistry;
import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.registry.TaskErrorRegistry;
import com.example.tgbot.registry.TaskResultRegistry;
import com.example.tgbot.service.PriceRegistryService;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import com.example.tgbot.util.ErrorMessageHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class CallbackHandler {

    private final ButtonRegistry buttonRegistry;
    private final PanelRegistry panelRegistry;
    private final SessionRegistry sessionRegistry;
    private final TaskResultRegistry taskResultRegistry;
    private final TaskErrorRegistry taskErrorRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;
    private final PriceRegistryService priceRegistryService;

    public CallbackHandler(@Lazy ButtonRegistry buttonRegistry, @Lazy PanelRegistry panelRegistry,
                           @Lazy SessionRegistry sessionRegistry, @Lazy TaskResultRegistry taskResultRegistry,
                           TaskErrorRegistry taskErrorRegistry, UserService userService,
                           PriceRegistryService priceRegistryService) {
        this.buttonRegistry = buttonRegistry;
        this.panelRegistry = panelRegistry;
        this.sessionRegistry = sessionRegistry;
        this.taskResultRegistry = taskResultRegistry;
        this.taskErrorRegistry = taskErrorRegistry;
        this.userService = userService;
        this.priceRegistryService = priceRegistryService;
    }

    public void handleCallback(CallbackQuery cq, UserSession userSession) {
        log.trace("handleCallback -> CallbackData={}", cq.getData());
        String[] dataArray = cq.getData().split("::");
        ButtonType buttonType = ButtonType.valueOf(dataArray[0]);
        String[] parameters = dataArray.length > 1
                ? Arrays.copyOfRange(dataArray, 1, dataArray.length)
                : new String[0];

        IButton button = buttonRegistry.getButton(buttonType);
        button.executeOnCallback(userSession, parameters);
    }

    public void handleApiCallback(RecordInfoResponse response, GenerationModel model) {
        if (Objects.equals(response.getData().getState(), "failed")) {
            log.error("Response from model {} failed - {}", model, response);
            processFailedResponse(response.getData().getTaskId());
        } else if (Objects.equals(response.getData().getState(), "complete") || Objects.equals(response.getData().getState(), "success")) {
            List<String> urlResponses = new ArrayList<>();
            urlResponses.add(extractUrlFromRecordInfo(response));
            processUrlResponses(response.getData().getTaskId(), urlResponses, model);
        }
    }

    public void handleApiCallback(SunoInfoResponse response, GenerationModel model) {
        if (Objects.equals(response.getData().getCallbackType(), "failed")) {
            log.error("Response from model {} failed - {}", model, response);
            processFailedResponse(response.getData().getTaskId());
        } else if (Objects.equals(response.getData().getCallbackType(), "complete") || Objects.equals(response.getData().getCallbackType(), "success")) {
            List<String> urlResponses = new ArrayList<>();
            response.getData().getData().forEach(dataBlock -> urlResponses.add(dataBlock.getAudioUrl()));
            processUrlResponses(response.getData().getTaskId(), urlResponses, model);
        }
    }

    private void processUrlResponses(String taskId, List<String> urlResponses, GenerationModel model) {
        TaskSource source = sessionRegistry.getTaskSource(taskId);
        try {
            UserSession session = sessionRegistry.getWaitingSession(taskId);
            if (session != null) session.touch();
            IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(taskId);
            Long userId = session.getUser().getTelegramId();
            if (userId != null) {
                int price = priceRegistryService.calculatePrice(requestOptions.getModel(), requestOptions, session.getUser());
                taskResultRegistry.put(taskId, new TaskResultRegistry.TaskResultRecord(
                        userId,
                        model,
                        requestOptions.convertToDTO(),
                        urlResponses,
                        price
                ));
            }
            if (source == TaskSource.CHAT) {
                session.setReceivedFile(ReceivedFile.builder()
                        .fileUrls(urlResponses)
                        .model(model)
                        .requestOptions(requestOptions)
                        .build());
            }
            session.removeRequestConfigurationAfterTaskCompletion(taskId);
            sessionRegistry.removeWaitingSession(taskId);
            int price = priceRegistryService.calculatePrice(requestOptions.getModel(), requestOptions, session.getUser());
            var costRub = priceRegistryService.getCostRub(requestOptions.getModel(), requestOptions);
            userService.consumeOneGeneration(session, price, requestOptions.getRequestInput(), urlResponses, requestOptions.getModel(), costRub);
            if (source == TaskSource.CHAT) {
                panelRegistry.getChatPanel(PanelType.MAIN_SEND_READY_FILE).execute(session);
            }
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            try {
                UserSession session = sessionRegistry.getWaitingSession(taskId);
                if (session != null) {
                    IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(taskId);
                    int price = priceRegistryService.calculatePrice(requestOptions.getModel(), requestOptions, session.getUser());
                    userService.rechargeFromHold(session, price, requestOptions.getRequestInput());
                    Long userId = session.getUser().getTelegramId();
                    if (userId != null) taskErrorRegistry.put(taskId, userId, ErrorCode.E007);
                    if (source == TaskSource.CHAT) {
                        session.setContextualMessage(ErrorMessageHelper.forTelegram(Operation.fromModel(requestOptions.getModel()), ErrorCode.E007));
                        panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
                    }
                    sessionRegistry.removeWaitingSession(taskId);
                }
            } catch (Exception ex) {
                log.error("Error handling parse failure", ex);
            }
        }
    }

    private void processFailedResponse(String taskId) {
        UserSession session = sessionRegistry.getWaitingSession(taskId);
        if (session == null) {
            log.error("No session for failed taskId={}", taskId);
            return;
        }
        TaskSource source = sessionRegistry.getTaskSource(taskId);
        session.touch();
        IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(taskId);
        int price = priceRegistryService.calculatePrice(requestOptions.getModel(), requestOptions, session.getUser());
        userService.rechargeFromHold(session, price, requestOptions.getRequestInput());

        ErrorCode errorCode = ErrorCode.E011;
        Long userId = session.getUser().getTelegramId();
        if (userId != null) {
            taskErrorRegistry.put(taskId, userId, errorCode);
        }
        sessionRegistry.removeWaitingSession(taskId);
        if (source == TaskSource.CHAT) {
            Operation operation = Operation.fromModel(requestOptions.getModel());
            session.setContextualMessage(ErrorMessageHelper.forTelegram(operation, errorCode));
            panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
        }
    }

    private String extractUrlFromRecordInfo(RecordInfoResponse resp) {
        RecordInfoResponse.DataBlock d = resp.getData();
        if (d == null || d.getResultJson() == null || d.getResultJson().isBlank()) {
            throw new IllegalStateException("recordInfo has no data/resultJson: " + resp);
        }

        String resultJsonStr = d.getResultJson();
        try {
            JsonNode root = objectMapper.readTree(resultJsonStr);

            JsonNode urls = root.path("resultUrls");
            if (urls.isArray() && !urls.isEmpty()) {
                String url = urls.get(0).asText(null);
                if (url != null && !url.isBlank()) return url;
            }


            JsonNode wm = root.path("resultWaterMarkUrls");
            if (wm.isArray() && wm.size() > 0) {
                String url = wm.get(0).asText(null);
                if (url != null && !url.isBlank()) return url;
            }

            throw new IllegalStateException("No result url in resultJson: " + resultJsonStr);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse resultJson: " + resultJsonStr, e);
        }
    }

}
