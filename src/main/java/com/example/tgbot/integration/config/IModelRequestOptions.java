package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;

import java.util.Map;

public interface IModelRequestOptions {
    Map<String, Object> getRequestInput();
    String getOptionsText();
    void setParametersFromJson(String json);
    String getPrompt();
    GenerationModel getModel();
    String convertToDTO();
}
