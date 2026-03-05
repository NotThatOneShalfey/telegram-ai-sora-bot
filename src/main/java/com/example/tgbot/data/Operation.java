package com.example.tgbot.data;

import com.example.tgbot.models.enums.GenerationModel;
import lombok.Getter;

/**
 * Операции для пользовательских сообщений об ошибках.
 */
@Getter
public enum Operation {
    VIDEO_GENERATION("генерацию видео"),
    MUSIC_GENERATION("генерацию музыки"),
    IMAGE_GENERATION("генерацию изображений"),
    FILE_UPLOAD("загрузку файлов");

    private final String displayName;

    Operation(String displayName) {
        this.displayName = displayName;
    }

    public static Operation fromModel(GenerationModel model) {
        return switch (model) {
            case KLING_3_0, SORA_2, SORA_2_WITH_IMAGE -> VIDEO_GENERATION;
            case SUNO_V5 -> MUSIC_GENERATION;
            case NANO_BANANA_PRO -> IMAGE_GENERATION;
        };
    }
}
