package com.example.tgbot.models.adapters;

import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.sessions.UserSession;

public interface IRequestAdapter {
    void makeRequest(UserSession session);
    GenerationModel getModel();
}
