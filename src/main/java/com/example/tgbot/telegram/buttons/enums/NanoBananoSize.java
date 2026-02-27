package com.example.tgbot.telegram.buttons.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum NanoBananoSize {

    SIZE_1_1("1:1", "1:1"),
    SIZE_9_16("9:16", "\uD83D\uDCF1 Вертикальное"),
    SIZE_16_9("16:9", "\uD83D\uDDA5️ Горизонтальное"),
    SIZE_3_4("3:4", "3:4"),
    SIZE_4_3("4:3", "4:3"),
    SIZE_3_2("3:2", "3:2"),
    SIZE_2_3("2:3", "2:3"),
    SIZE_5_4("5:4", "5:4"),
    SIZE_4_5("4:5", "4:5"),
    SIZE_21_9("21:9", "21:9");
    String value;
    String buttonText;

    NanoBananoSize(String value, String buttonText) {
        this.value = value;
        this.buttonText = buttonText;

    }
}
