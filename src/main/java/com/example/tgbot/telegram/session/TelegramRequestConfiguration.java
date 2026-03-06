package com.example.tgbot.telegram.session;

import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.domain.enums.GenerationModel;
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
