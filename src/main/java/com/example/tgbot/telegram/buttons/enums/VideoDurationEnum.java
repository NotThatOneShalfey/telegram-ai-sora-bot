package com.example.tgbot.telegram.buttons.enums;

import lombok.Getter;

@Getter
public enum VideoDurationEnum {
    DURATION_4(4, "⏱ 4 сек"),
    DURATION_6(6, "⏱ 6 сек"),
    DURATION_8(8, "⏱ 8 сек"),
    DURATION_10(10,"⏱ 10 сек"),
    DURATION_12(12,"⏱ 12 сек"),
    DURATION_14(14,"⏱ 14 сек");

    private final Integer value;
    private final String buttonText;

    VideoDurationEnum(Integer value, String buttonText) {
        this.value = value;
        this.buttonText = buttonText;
    }
}
