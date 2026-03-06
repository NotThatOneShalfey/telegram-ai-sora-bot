package com.example.tgbot.models.configurations;

import com.example.tgbot.models.enums.GenerationModel;

import java.util.Map;

public interface IModelRequestOptions {
    Map<String, Object> getRequestInput();
    String getOptionsText();
    void setParametersFromJson(String json);
    String getPrompt();
    GenerationModel getModel();
    String convertToDTO();
}
