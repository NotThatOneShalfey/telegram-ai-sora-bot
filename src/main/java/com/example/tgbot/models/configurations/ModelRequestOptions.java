package com.example.tgbot.models.configurations;

import java.util.HashMap;
import java.util.Map;

public interface ModelRequestOptions {
    int getPrice();

    Map<String, Object> getRequestInput();
    String getOptionsText();
}
