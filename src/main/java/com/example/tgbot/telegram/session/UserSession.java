package com.example.tgbot.telegram.session;

import com.example.tgbot.domain.model.User;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.*;
import com.example.tgbot.integration.kieai.ReceivedFile;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.button.enums.PaidPackageEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@ToString
public class UserSession {
    @Setter
    private User user;
    private final String chatId;
    @Setter
    private LocalDateTime lastActionDateTime;
    @Setter
    private PaidPackageEnum paymentInfo = null;
    @Setter
    private ChatContext chatContext;
    @Setter
    private ReceivedFile receivedFile;
    /** Источник запроса (WEB/CHAT) — задаётся перед вызовом makeRequest, используется для изоляции чата и веба */
    @Setter
    private TaskSource requestSource;
    private final List<TelegramRequestConfiguration> requestConfigurationList = new ArrayList<>();

    public UserSession(User user) {
        this.user = user;
        this.chatId = user.getTelegramId().toString();
        this.chatContext = new ChatContext(ChatState.INITIAL);
        this.lastActionDateTime = LocalDateTime.now();

        // Инициализация дефолтных пресетов опций
        createNewModelRequestConfiguration(GenerationModel.KLING_3_0, KlingOptions.builder().build());
        createNewModelRequestConfiguration(GenerationModel.SORA_2, SoraOptions.builder().build());
        createNewModelRequestConfiguration(GenerationModel.SUNO_V5, SunoOptions.builder().build());
        createNewModelRequestConfiguration(GenerationModel.NANO_BANANA_PRO, NanoBananaOptions.builder().build());
    }

    public IModelRequestOptions getCurrentRequestOptionsByModel(GenerationModel model) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                return configuration.getRequestOptions();
            }
        }
        return null;
    }

    public IModelRequestOptions getRequestOptionsByTaskIdAndModel(String taskId) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (Objects.equals(configuration.getTaskId(), taskId)) {
                return configuration.getRequestOptions();
            }
        }
        return null;
    }

    public void setTaskIdForCurrentModelConfiguration(String taskId, GenerationModel model) {
        IModelRequestOptions options = null;
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                // Выставляем и сразу же дополняем список дефолтным с тем же опциями
                configuration.setTaskId(taskId);
                options = configuration.getRequestOptions();
            }
        }
        if (options != null) {
            createNewModelRequestConfiguration(model, options);
        }
    }

    public void createNewModelRequestConfiguration(GenerationModel model, IModelRequestOptions options) {
        TelegramRequestConfiguration currentConfiguration = null;
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                currentConfiguration = configuration;
            }
        }
        if (currentConfiguration == null) {
            requestConfigurationList.add(TelegramRequestConfiguration.builder().requestOptions(options).model(model).build());
        }
    }

    public void removeRequestConfigurationAfterTaskCompletion(String taskId) {
        requestConfigurationList.removeIf(config -> Objects.equals(taskId, config.getTaskId()));
    }

    public void setContextualMessage(String message) {
        chatContext.setContextualMessage(message);
    }

    public String getContextualMessage() {
        return chatContext.getContextualMessage();
    }

    /** Обновляет lastActionDateTime текущим временем. Вызывать при любой операции с сессией. */
    public void touch() {
        this.lastActionDateTime = LocalDateTime.now();
    }

    /** Источник запроса; по умолчанию CHAT (если не задан). */
    public TaskSource getRequestSource() {
        return requestSource != null ? requestSource : TaskSource.CHAT;
    }

}
