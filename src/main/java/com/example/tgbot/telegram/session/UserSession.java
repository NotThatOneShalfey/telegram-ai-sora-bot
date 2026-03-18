package com.example.tgbot.telegram.session;

import com.example.tgbot.domain.model.User;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.KlingMotionControlOptions;
import com.example.tgbot.integration.config.ElevenLabsOptions;
import com.example.tgbot.integration.config.RestoredRequestOptions;
import com.example.tgbot.integration.config.SeedanceImageToVideoOptions;
import com.example.tgbot.integration.config.*;
import com.example.tgbot.integration.kieai.ReceivedFile;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.button.enums.PaidPackageEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    /** Восстановленные из БД ожидающие задачи (taskId → опции). Используется после restore в getRequestOptionsByTaskIdAndModel. */
    private final Map<String, RestoredRequestOptions> restoredPendingTasks = new ConcurrentHashMap<>();

    public UserSession(User user) {
        this.user = user;
        this.chatId = user.getTelegramId().toString();
        this.chatContext = new ChatContext(ChatState.INITIAL);
        this.lastActionDateTime = LocalDateTime.now();

        // Инициализация дефолтных пресетов опций
        createNewModelRequestConfiguration(GenerationModel.KLING_3_0, KlingOptions.builder().build());
        createNewModelRequestConfiguration(GenerationModel.KLING_3_MOTION_CONTROL, KlingMotionControlOptions.builder().build());
        createNewModelRequestConfiguration(GenerationModel.SEEDANCE_2_0, SeedanceImageToVideoOptions.builder().build());
        createNewModelRequestConfiguration(GenerationModel.ELEVENLABS_V3, ElevenLabsOptions.builder().build());
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
        RestoredRequestOptions restored = restoredPendingTasks.get(taskId);
        if (restored != null) {
            return restored;
        }
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (Objects.equals(configuration.getTaskId(), taskId)) {
                return configuration.getRequestOptions();
            }
        }
        return null;
    }

    /** Добавляет восстановленную из БД ожидающую задачу (вызывается при restore). */
    public void putRestoredPendingTask(String taskId, GenerationModel model, Map<String, Object> requestInput) {
        restoredPendingTasks.put(taskId, new RestoredRequestOptions(model, requestInput != null ? requestInput : Map.of()));
    }

    /** Удаляет восстановленную задачу после завершения (вызвать из callback при removeWaitingSession). */
    public void removeRestoredPendingTask(String taskId) {
        restoredPendingTasks.remove(taskId);
    }

    public void setOperationsHistoryIdForCurrentModel(GenerationModel model, UUID operationsHistoryId) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                configuration.setOperationsHistoryId(operationsHistoryId);
                break;
            }
        }
    }

    public UUID getOperationsHistoryIdByTaskId(String taskId) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (Objects.equals(configuration.getTaskId(), taskId)) {
                return configuration.getOperationsHistoryId();
            }
        }
        return null;
    }

    /** Возвращает operationsHistoryId для текущей конфигурации модели (до вызова адаптера) */
    public UUID getOperationsHistoryIdForCurrentModel(GenerationModel model) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() == null) {
                return configuration.getOperationsHistoryId();
            }
        }
        return null;
    }

    /** Возвращает taskId для конфигурации модели (после успешного вызова адаптера) */
    public String getTaskIdForModel(GenerationModel model) {
        for (TelegramRequestConfiguration configuration : requestConfigurationList) {
            if (configuration.getModel().equals(model) && configuration.getTaskId() != null) {
                return configuration.getTaskId();
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
