package com.example.tgbot.models.adapters;

import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.sessions.UserSession;

import java.util.Optional;

public interface IRequestAdapter {
    /** Возвращает taskId при успешной постановке задачи, empty при ошибке. */
    Optional<String> makeRequest(UserSession session);
    GenerationModel getModel();
}
