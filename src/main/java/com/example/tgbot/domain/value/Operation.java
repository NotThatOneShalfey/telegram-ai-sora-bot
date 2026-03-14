package com.example.tgbot.domain.value;

import com.example.tgbot.domain.enums.GenerationModel;
import lombok.Getter;

/**
 * Операции для пользовательских сообщений об ошибках.
 */
@Getter
public enum Operation {
    VIDEO_GENERATION("генерацию видео"),
    AUDIO_GENERATION("генерацию аудио"),
    IMAGE_GENERATION("генерацию изображений"),
    FILE_UPLOAD("загрузку файлов");

    private final String displayName;

    Operation(String displayName) {
        this.displayName = displayName;
    }

    public static Operation fromModel(GenerationModel model) {
        return switch (model) {
            case KLING_3_0, KLING_3_MOTION_CONTROL, SEEDANCE_2_0, SORA_2, SORA_2_WITH_IMAGE -> VIDEO_GENERATION;
            case SUNO_V5, ELEVENLABS_V3 -> AUDIO_GENERATION;
            case NANO_BANANA_PRO -> IMAGE_GENERATION;
        };
    }
}
