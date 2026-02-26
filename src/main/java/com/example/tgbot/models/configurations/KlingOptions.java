package com.example.tgbot.models.configurations;

import com.example.tgbot.models.enums.GenerationModel;
import lombok.Builder;
import lombok.Setter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Builder
@Setter
public class KlingOptions implements ModelRequestOptions {

    @Builder.Default
    private final GenerationModel model = GenerationModel.KLING_3_0;
    private String aspect_ratio;
    @Builder.Default
    private int duration = 10;
    @Builder.Default
    private boolean withSound = false;
    @Builder.Default
    private String mode = "std";
    private boolean multiShots;
    private String[] image_urls;
    private String prompt;
    @Builder.Default
    private List<MultiShotRequest> multiShotRequestArray = new ArrayList<>();

    @Override
    public int getPrice() {
        return 0;
    }

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("mode", mode);
        input.put("image_urls", image_urls);
        input.put("aspect_ratio", aspect_ratio);
        if (multiShots) {
            input.put("multi_prompt", multiShotRequestArray);
        } else {
            input.put("prompt", prompt);
            input.put("duration", duration);
        }
        return input;
    }

    @Override
    public String getOptionsText() {
        String text = """
                
                ПАРАМЕТРЫ
                Модель: {0}
                Формат: {1}
                Длительность: {2}
                Звук: {3}
                Режим: {4}
                Мультикадр: {5}
                
                """;

        return MessageFormat.format(text,
                model.getLocalizedModelName(),
                getLocalizedAspectRatio(),
                duration,
                withSound ? "Включен" : "Выключен",
                mode.equalsIgnoreCase("std") ? "Стандарт" : "Про",
                multiShots ? "Включен" : "Выключен"
                );
    }

    @Builder
    private static class MultiShotRequest {
        String prompt;
        int duration;
    }

    private String getLocalizedAspectRatio() {
        if (this.aspect_ratio.equalsIgnoreCase("9:16")) {
            return "\uD83D\uDCF1 Вертикальное";
        } else if (this.aspect_ratio.equalsIgnoreCase("16:9")) {
            return "\uD83D\uDDA5️ Горизонтальное";
        } else {
            return aspect_ratio;
        }
    }

}
