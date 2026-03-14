package com.example.tgbot.telegram.session;

import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.domain.enums.GenerationModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class TelegramRequestConfiguration {
    @Setter
    @Builder.Default
    private String taskId = null;
    private GenerationModel model;
    private IModelRequestOptions requestOptions;
    /** ID записи в operations_history для отслеживания lifecycle генерации */
    @Setter
    private UUID operationsHistoryId;
}
