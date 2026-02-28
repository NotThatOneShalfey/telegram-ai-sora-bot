package com.example.tgbot.telegram.sessions;

import com.example.tgbot.db.User;
import com.example.tgbot.models.configurations.ModelRequestOptions;
import com.example.tgbot.models.data.ReceivedFile;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@ToString
public class UserSession {

    private final User user;
    private final String chatId;
    @Setter
    private LocalDateTime lastActionDateTime;
    private final Map<GenerationModel, ModelRequestOptions> modelsConfiguration = new HashMap<>();
    @Setter
    private PaidPackageEnum paymentInfo = null;
    @Setter
    private ChatContext chatContext;
    @Setter
    private ReceivedFile receivedFile;
    private final List<TelegramRequestConfiguration> requestConfigurationList = new ArrayList<>();

    public UserSession(User user) {
        this.user = user;
        this.chatId = user.getTelegramId().toString();
        this.chatContext = new ChatContext(ChatState.INITIAL);
    }

    public ModelRequestOptions getCurrentRequestOptionsByModel(GenerationModel model) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                return configuration.getRequestOptions();
            }
        }
        return null;
    }

    public ModelRequestOptions getRequestOptionsByTaskIdAndModel(String taskId, GenerationModel model) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && Objects.equals(configuration.getTaskId(), taskId)) {
                return configuration.getRequestOptions();
            }
        }
        return null;
    }

    public void setTaskIdForCurrentModelConfiguration(String taskId, GenerationModel model) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                configuration.setTaskId(taskId);
            }
        }
    }

    public void createNewModelRequestConfiguration(GenerationModel model, ModelRequestOptions options) {
        TelegramRequestConfiguration currentConfiguration = null;

        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                currentConfiguration = configuration;
            }
        }
        if (currentConfiguration != null) {
            currentConfiguration.toBuilder().requestOptions(options).build();
        } else {
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

}
