package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Минимальная реализация IModelRequestOptions для восстановленных из БД ожидающих задач.
 * Используется при restore: callback получает модель и request input для расчёта цены и завершения операции.
 */
@RequiredArgsConstructor
public class RestoredRequestOptions implements IModelRequestOptions {

    private final GenerationModel model;
    private final Map<String, Object> requestInput;

    @Override
    public Map<String, Object> getRequestInput() {
        return requestInput != null ? requestInput : Collections.emptyMap();
    }

    @Override
    public GenerationModel getModel() {
        return model;
    }

    @Override
    public String getOptionsText() {
        return "";
    }

    @Override
    public String getPrompt() {
        Object p = requestInput != null ? requestInput.get("prompt") : null;
        return p != null ? p.toString() : null;
    }

    @Override
    public void setParametersFromJson(String json) {
        // no-op для восстановленных опций
    }

    @Override
    public String convertToDTO() {
        return "{}";
    }
}
