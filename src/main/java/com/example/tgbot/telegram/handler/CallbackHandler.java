package com.example.tgbot.telegram.handler;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.domain.value.Operation;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.kieai.ReceivedFile;
import com.example.tgbot.integration.kieai.RecordInfoResponse;
import com.example.tgbot.integration.seedance.SeedanceRecordInfoResponse;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        RecordInfoResponse.DataBlock data = response.getData();
        ErrorCode errorCode = errorCodeForModel(model);
        if (data == null || isErrorCode(response.getCode())) {
            log.error("Response from model {} failed (code={}) - {}", model, response.getCode(), response);
            processFailedResponse(data != null ? data.getTaskId() : null, errorCode);
            return;
        }
        String state = data.getState();
        if ("failed".equals(state) || "fail".equals(state)) {
            log.error("Response from model {} failed - {}", model, response);
            processFailedResponse(data.getTaskId(), errorCode);
        } else if ("complete".equals(state) || "success".equals(state)) {
            String url = extractUrlFromRecordInfo(response);
            List<Object> resultItems = Collections.singletonList(Map.<String, Object>of("url", url));
            processResultResponses(data.getTaskId(), resultItems, List.of(url), model);
        }
    }

    /** Обработка результата polling Seedance (success/fail). */
    public void handleSeedancePollResult(String taskId, String status,
                                         List<SeedanceRecordInfoResponse.OutputItem> output, String error) {
        if ("success".equals(status) && output != null && !output.isEmpty()) {
            String url = output.get(0).getUrl();
            if (url != null && !url.isBlank()) {
                List<Object> resultItems = Collections.singletonList(Map.<String, Object>of("url", url));
                processResultResponses(taskId, resultItems, List.of(url), GenerationModel.SEEDANCE_2_0);
            }
        } else if ("fail".equals(status)) {
            processFailedResponse(taskId, ErrorCode.E001);
        }
    }

    public void handleApiCallback(SunoInfoResponse response, GenerationModel model) {
        SunoInfoResponse.SunoData data = response.getData();
        if (data == null || isErrorCode(response.getCode())) {
            log.error("Response from model {} failed (code={}, msg={}) - {}", model, response.getCode(), response.getMessage(), response);
            processFailedResponse(data != null ? data.getTaskId() : null, ErrorCode.E002);
            return;
        }
        String callbackType = data.getCallbackType();
        if ("failed".equals(callbackType) || "error".equals(callbackType)) {
            log.error("Response from model {} failed - {}", model, response);
            processFailedResponse(data.getTaskId(), ErrorCode.E002);
            return;
        }
        if ("complete".equals(callbackType) || "success".equals(callbackType)) {
            var dataBlocks = data.getData();
            if (dataBlocks == null || dataBlocks.isEmpty()) {
                log.error("Response from model {} failed: callbackType=complete but data is empty (code={}) - {}", model, response.getCode(), response);
                processFailedResponse(data.getTaskId(), ErrorCode.E002);
                return;
            }
            List<Object> sunoItems = new ArrayList<>();
            List<String> audioUrls = new ArrayList<>();
            for (SunoInfoResponse.DataBlock db : dataBlocks) {
                Map<String, Object> item = new HashMap<>();
                item.put("audioUrl", db.getAudioUrl() != null ? db.getAudioUrl() : "");
                item.put("imageUrl", db.getImageUrl() != null ? db.getImageUrl() : "");
                item.put("title", db.getTitle() != null ? db.getTitle() : "");
                item.put("text", db.getPrompt() != null ? db.getPrompt() : "");
                sunoItems.add(item);
                if (db.getAudioUrl() != null && !db.getAudioUrl().isBlank()) {
                    audioUrls.add(db.getAudioUrl());
                }
            }
            processResultResponses(data.getTaskId(), sunoItems, audioUrls, model);
        }
    }

    private static boolean isErrorCode(int code) {
        return code != 200;
    }

    private static ErrorCode errorCodeForModel(GenerationModel model) {
        return switch (model != null ? model.getGenerationType() : null) {
            case VIDEO -> ErrorCode.E001;
            case IMAGE -> ErrorCode.E003;
            case AUDIO -> ErrorCode.E002;
            default -> ErrorCode.E011;
        };
    }

    private void processResultResponses(String taskId, List<Object> resultItems, List<String> fileUrls, GenerationModel model) {
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
                        resultItems,
                        price
                ));
            }
            if (source == TaskSource.CHAT) {
                session.setReceivedFile(ReceivedFile.builder()
                        .fileUrls(fileUrls)
                        .model(model)
                        .requestOptions(requestOptions)
                        .build());
            }
            session.removeRequestConfigurationAfterTaskCompletion(taskId);
            sessionRegistry.removeWaitingSession(taskId);
            int price = priceRegistryService.calculatePrice(requestOptions.getModel(), requestOptions, session.getUser());
            var costRub = priceRegistryService.getCostRub(requestOptions.getModel(), requestOptions);
            session.setUser(userService.consumeOneGeneration(session, price, requestOptions.getRequestInput(), resultItems, requestOptions.getModel(), costRub, taskId));
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
                    userService.updateGenerationHistoryToFailed(taskId);
                    session.setUser(userService.rechargeFromHold(session, price, requestOptions.getRequestInput()));
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

    private void processFailedResponse(String taskId, ErrorCode errorCode) {
        if (taskId == null) {
            log.warn("processFailedResponse: taskId is null, cannot process");
            return;
        }
        UserSession session = sessionRegistry.getWaitingSession(taskId);
        if (session == null) {
            log.error("No session for failed taskId={}", taskId);
            return;
        }
        TaskSource source = sessionRegistry.getTaskSource(taskId);
        session.touch();
        IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(taskId);
        int price = priceRegistryService.calculatePrice(requestOptions.getModel(), requestOptions, session.getUser());
        userService.updateGenerationHistoryToFailed(taskId);
        session.setUser(userService.rechargeFromHold(session, price, requestOptions.getRequestInput()));

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
            if (wm.isArray() && !wm.isEmpty()) {
                String url = wm.get(0).asText(null);
                if (url != null && !url.isBlank()) return url;
            }

            throw new IllegalStateException("No result url in resultJson: " + resultJsonStr);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse resultJson: " + resultJsonStr, e);
        }
    }

}
