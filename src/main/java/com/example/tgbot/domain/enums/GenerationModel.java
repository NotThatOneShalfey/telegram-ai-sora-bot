package com.example.tgbot.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GenerationModel {
    KLING_3_0("kling-3.0/video", "Kling 3.0"),
    KLING_3_MOTION_CONTROL("kling-3.0/motion-control", "Kling 3.0 Motion Control"),
    SEEDANCE_2_0("seedance-2.0", "Seedance 2.0"),
    NANO_BANANA_PRO("nano-banana-pro", "Nano Banana Pro"),
    SORA_2("sora-2-text-to-video-stable", "Sora 2"),
    SORA_2_WITH_IMAGE("sora-2-image-to-video-stable", "Sora 2"),
    SUNO_V5("V5", "Suno V5");

    private final String requestModelName;
    private final String localizedModelName;

    /** Маппинг модели на тип контента для фильтрации истории. */
    public GenerationType getGenerationType() {
        return switch (this) {
            case KLING_3_0, KLING_3_MOTION_CONTROL, SEEDANCE_2_0, SORA_2, SORA_2_WITH_IMAGE -> GenerationType.VIDEO;
            case SUNO_V5 -> GenerationType.MUSIC;
            case NANO_BANANA_PRO -> GenerationType.IMAGE;
        };
    }
}
