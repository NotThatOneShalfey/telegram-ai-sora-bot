package com.example.tgbot.telegram.buttons;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AspectRatioButton {
    FORMAT_16_9("format_16_9", "16:9", "\uD83D\uDDA5️ Горизонтальное"),
    FORMAT_9_16("format_9_16", "9:16", "\uD83D\uDCF1 Вертикальное");

    String buttonCallback;
    String buttonValueForOptions;
    String buttonText;

    AspectRatioButton(String buttonCallback, String buttonValueForOptions, String buttonText) {
        this.buttonCallback = buttonCallback;
        this.buttonValueForOptions = buttonValueForOptions;
        this.buttonText = buttonText;
    }

    public static AspectRatioButton getAspectRatioByCallback(String cb) {
        Optional<AspectRatioButton> pack = Arrays.stream(AspectRatioButton.values()).filter(pp -> pp.buttonCallback.equalsIgnoreCase(cb)).findFirst();
        return pack.orElse(null);
    }


}
