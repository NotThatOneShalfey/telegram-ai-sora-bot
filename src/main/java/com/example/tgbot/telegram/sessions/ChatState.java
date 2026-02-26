package com.example.tgbot.telegram.sessions;

import com.example.tgbot.models.enums.GenerationModel;
import lombok.Getter;

@Getter
public enum ChatState {
    INITIAL,
    WAITING_FOR_IMAGE,
    WAITING_FOR_TEXT;
}
