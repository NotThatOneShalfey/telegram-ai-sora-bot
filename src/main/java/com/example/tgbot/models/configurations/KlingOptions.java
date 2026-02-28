package com.example.tgbot.models.configurations;

import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.enums.AspectRatioEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Builder
@Setter
@ToString
public class KlingOptions implements ModelRequestOptions {

    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    @Getter
    private final GenerationModel model = GenerationModel.KLING_3_0;
    @Builder.Default
    private String aspect_ratio = "9:16";
    @Builder.Default
    private int duration = 10;
    @Builder.Default
    private boolean withSound = false;
    @Builder.Default
    private String mode = "std";
    @Builder.Default
    private boolean multiShots = false;
    private String[] imageUrls;
    @Getter
    private String prompt;
    @Builder.Default
    private List<MultiShotRequest> multiShotRequestArray = new ArrayList<>();

    @Override
    public int getPrice() {
        double resultingPrice = 7.66;
        if (mode.equalsIgnoreCase("pro")) {
            if (withSound) {
                resultingPrice = 15.33;
            } else {
                resultingPrice = 9.96;
            }
        } else if (mode.equalsIgnoreCase("std")) {
            if (withSound) {
                resultingPrice = 11.49;
            } else {
                resultingPrice = 7.66;
            }
        }
        return Math.round((float) (resultingPrice * duration * 1.5F));
    }

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("mode", mode);
        input.put("image_urls", imageUrls);
        input.put("aspect_ratio", aspect_ratio);
        if (multiShots) {
            input.put("multi_prompt", multiShotRequestArray);
        } else {
            input.put("prompt", prompt);
            input.put("duration", duration);
        }
        input.put("multi_shots", multiShots);
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
                AspectRatioEnum.getButtonTextByValue(aspect_ratio),
                duration + " секунд",
                withSound ? "Включен" : "Выключен",
                mode.equalsIgnoreCase("std") ? "Стандарт" : "Про",
                multiShots ? "Включен" : "Выключен"
                );
    }

    @Override
    public void setParametersFromJson(String json) {
        try {
            mapper.updateValue(this, mapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Builder
    private static class MultiShotRequest {
        String prompt;
        int duration;
    }

}
