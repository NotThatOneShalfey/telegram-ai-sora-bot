package com.example.tgbot.telegram.handlers;

import com.example.tgbot.registry.ButtonRegistry;
import com.example.tgbot.registry.PanelRegistry;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.registry.TaskResultRegistry;
import com.example.tgbot.service.ImageUploadService;
import com.example.tgbot.util.UploadedImageUrlsExtractor;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.data.ReceivedFile;
import com.example.tgbot.models.data.RecordInfoResponse;
import com.example.tgbot.models.data.SunoInfoResponse;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler {

    private final ButtonRegistry buttonRegistry;
    private final PanelRegistry panelRegistry;
    private final SessionRegistry sessionRegistry;
    private final TaskResultRegistry taskResultRegistry;
    private final UploadedImageUrlsExtractor uploadedImageUrlsExtractor;
    private final ImageUploadService imageUploadService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

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
        try {
            // Получаем сессию из ожидающих ответа
            UserSession session = sessionRegistry.getWaitingSession(taskId);
            // Получаем с какими опциями мы делали
            IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(taskId);
            // Сохраняем результат для web-интерфейса (запрос по userName + taskId)
            String userName = session.getUser().getUserName();
            if (userName != null && !userName.isBlank()) {
                taskResultRegistry.put(taskId, new TaskResultRegistry.TaskResultRecord(
                        userName,
                        model,
                        requestOptions.convertToDTO(),
                        urlResponses
                ));
            }
            // Складываем в инфу о текущем отправляемом файле
            session.setReceivedFile(ReceivedFile.builder()
                    .fileUrls(urlResponses)
                    .model(model)
                    .requestOptions(requestOptions)
                    .build());
            // Убираем настройки с заданием
            session.removeRequestConfigurationAfterTaskCompletion(taskId);
            // И убираем сессию из списка ожидающих
            sessionRegistry.removeWaitingSession(taskId);
            // Снимаем деньги
            userService.consumeOneGeneration(session, requestOptions.getPrice(), requestOptions.getRequestInput());
            // Удаляем загруженные изображения после успешной генерации
            deleteUploadedImages(requestOptions);
            // Вызываем панель для отправки файла
            panelRegistry.getChatPanel(PanelType.MAIN_SEND_READY_FILE).execute(session);
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
        }
    }

    private void processFailedResponse(String taskId) {
        UserSession session = sessionRegistry.getWaitingSession(taskId);
        IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(taskId);
        userService.rechargeFromHold(session, requestOptions.getPrice(), requestOptions.getRequestInput());
        deleteUploadedImages(requestOptions);
        session.setContextualMessage("Не удалось обработать запрос. На ваш счет вернулись монеты. Просим обратиться в поддержку.");
        panelRegistry.getChatPanel(PanelType.MAIN_SIMPLE_MESSAGE).execute(session);
    }

    private void deleteUploadedImages(IModelRequestOptions requestOptions) {
        try {
            var urls = uploadedImageUrlsExtractor.extractOurUploadedUrls(requestOptions);
            if (!urls.isEmpty()) {
                imageUploadService.deleteByUrls(urls);
            }
        } catch (Exception e) {
            log.warn("Failed to delete uploaded images: {}", e.getMessage());
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
            if (urls.isArray() && urls.size() > 0) {
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
