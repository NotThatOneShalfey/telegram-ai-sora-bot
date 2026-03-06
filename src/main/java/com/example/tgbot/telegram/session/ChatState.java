package com.example.tgbot.telegram.session;

import lombok.Getter;

@Getter
public enum ChatState {
    INITIAL,
    WAITING_FOR_IMAGE,
    WAITING_FOR_TEXT
}
