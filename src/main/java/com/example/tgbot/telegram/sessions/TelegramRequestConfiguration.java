package com.example.tgbot.telegram.sessions;

import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.enums.GenerationModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(toBuilder = true)
public class TelegramRequestConfiguration {
    @Setter
    @Builder.Default
    private String taskId = null;
    private GenerationModel model;
    private IModelRequestOptions requestOptions;
}
