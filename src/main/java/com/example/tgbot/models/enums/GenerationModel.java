package com.example.tgbot.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GenerationModel {
    KLING_3_0("kling-3.0/video", "Kling 3.0"),
    NANO_BANANA_PRO("nano-banana-pro", "Nano Banana Pro"),
    SORA_2("sora-2-text-to-video-stable", "Sora 2"),
    SORA_2_WITH_IMAGE("sora-2-image-to-video-stable", "Sora 2"),
    SUNO_V5("V5", "Suno V5");
    //SEEDANCE_2_0();

    private final String requestModelName;
    private final String localizedModelName;

    public static GenerationModel getByRequestModelName(String requestModelName) {
        for (GenerationModel gm : GenerationModel.values()) {
            if (gm.getRequestModelName().equalsIgnoreCase(requestModelName)) {
                return gm;
            }
        }
        return null;
    }
}
