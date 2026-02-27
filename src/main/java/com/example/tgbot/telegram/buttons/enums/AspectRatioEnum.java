package com.example.tgbot.telegram.buttons.enums;

import lombok.Getter;

@Getter
public enum AspectRatioEnum {
    FORMAT_16_9("16:9", "\uD83D\uDDA5️ Горизонтальное"),
    FORMAT_9_16("9:16", "\uD83D\uDCF1 Вертикальное");

    final String value;
    final String buttonText;

    AspectRatioEnum(String value, String buttonText) {
        this.value = value;
        this.buttonText = buttonText;
    }

    public static String getButtonTextByValue(String value) {
        for (AspectRatioEnum are : AspectRatioEnum.values()) {
            if (are.getValue().equalsIgnoreCase(value)) {
                return are.getButtonText();
            }
        }
        return null;
    }


}
