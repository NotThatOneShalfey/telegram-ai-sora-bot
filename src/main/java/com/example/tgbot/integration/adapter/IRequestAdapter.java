package com.example.tgbot.integration.adapter;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.telegram.session.UserSession;

import java.util.Optional;

public interface IRequestAdapter {
    /** Возвращает taskId при успешной постановке задачи, empty при ошибке. */
    Optional<String> makeRequest(UserSession session);
    GenerationModel getModel();
}
