package com.example.tgbot.telegram.handlers;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.data.ReceivedFile;
import com.example.tgbot.models.data.RecordInfoResponse;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler {

    private final ObjectProvider<RegistryService> registryServiceProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    public void handleCallback(CallbackQuery cq, UserSession userSession) {
        log.trace("call handleCallback ---> CallbackData={}", cq.getData());
        String[] dataArray = cq.getData().split("::");
        String buttonType = dataArray[0];
        IButton button = registryServiceProvider.getObject().getButton(ButtonType.valueOf(buttonType));
        if (dataArray.length > 1) {
            for (int i=1; i<dataArray.length; i++) {
                button.setParameters(dataArray[i]);
            }
        }
        button.executeOnCallback(userSession);
    }

    public void handleApiCallback(RecordInfoResponse response, GenerationModel model) {
        try {
            List<String> urlResponses = new ArrayList<>();
            urlResponses.add(extractUrlFromRecordInfo(response));
            RegistryService registryService = registryServiceProvider.getObject();
            // Получаем сессию из ожидающих ответа
            UserSession session = registryService.getWaitingSession(response.getData().getTaskId());
            // Получаем с какими опциями мы делали
            IModelRequestOptions requestOptions = session.getRequestOptionsByTaskIdAndModel(response.getData().getTaskId());
            // Складываем в инфу о текущем отправляемом файле
            session.setReceivedFile(ReceivedFile.builder()
                    .fileUrls(urlResponses)
                    .model(model)
                    .requestOptions(requestOptions)
                    .build());
            // Вызываем панель для отправки файла
            registryService.getChatPanel(PanelType.MAIN_SEND_READY_FILE).execute(session);
            // Убираем настройки с заданием
            session.removeRequestConfigurationAfterTaskCompletion(response.getData().getTaskId());
            // И убираем сессию из списка ожидающих
            registryService.removeWaitingSession(response.getData().getTaskId());
            // Снимаем деньги
            userService.consumeOneGeneration(session, requestOptions.getPrice(), objectMapper.writeValueAsString(requestOptions.getRequestInput()));
        } catch (IllegalStateException | JsonProcessingException e) {
            log.error(e.getMessage());
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
