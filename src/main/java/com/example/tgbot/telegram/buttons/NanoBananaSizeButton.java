package com.example.tgbot.telegram.buttons;

import java.util.Arrays;
import java.util.Optional;

public enum NanoBananaSizeButton {

    SIZE_1_1("size_1_1", "1:1", "1:1"),
    SIZE_9_16("size_9_16", "9:16", "\uD83D\uDCF1 Вертикальное"),
    SIZE_16_9("size_16_9", "16:9", "\uD83D\uDDA5️ Горизонтальное"),
    SIZE_3_4("size_3_4", "3:4", "3:4"),
    SIZE_4_3("size_4_3", "4:3", "4:3"),
    SIZE_3_2("size_3_2", "3:2", "3:2"),
    SIZE_2_3("size_2_3", "2:3", "2:3"),
    SIZE_5_4("size_5_4", "5:4", "5:4"),
    SIZE_4_5("size_4_5", "4:5", "4:5"),
    SIZE_21_9("size_21_9", "21:9", "21:9");

    String buttonCallback;
    String buttonValueForOptions;
    String buttonText;

    NanoBananaSizeButton(String buttonCallback, String buttonValueForOptions, String buttonText) {
        this.buttonCallback = buttonCallback;
        this.buttonValueForOptions = buttonValueForOptions;
        this.buttonText = buttonText;

    }

    public static NanoBananaSizeButton getSizeByCallback(String cb) {
        Optional<NanoBananaSizeButton> pack = Arrays.stream(NanoBananaSizeButton.values()).filter(pp -> pp.buttonCallback.equalsIgnoreCase(cb)).findFirst();
        return pack.orElse(null);
    }
}
