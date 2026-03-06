package com.example.tgbot.models.configurations;

import com.example.tgbot.models.configurations.dto.KlingOptionsDTO;
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
public class KlingOptions implements IModelRequestOptions {

    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    @Getter
    private final GenerationModel model = GenerationModel.KLING_3_0;
    @Builder.Default
    private String aspectRatio = "9:16";
    @Builder.Default
    private int duration = 10;
    @Getter
    @Builder.Default
    private boolean withSound = false;
    @Getter
    @Builder.Default
    private String mode = "std";
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
    @Getter
    private String prompt;

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
        input.put("aspect_ratio", aspectRatio);
        input.put("sound", withSound);
        input.put("prompt", prompt);
        input.put("duration", duration);
        input.put("multi_shots", false);
        return input;
    }

    @Override
    public String getOptionsText() {
        String text = """
                <pre>
                ПАРАМЕТРЫ
                Модель: {0}
                Формат: {1}
                Длительность: {2}
                Звук: {3}
                Режим: {4}
                </pre>
                """;

        return MessageFormat.format(text,
                model.getLocalizedModelName(),
                AspectRatioEnum.getButtonTextByValue(aspectRatio),
                duration + " секунд",
                withSound ? "Включен" : "Выключен",
                mode.equalsIgnoreCase("std") ? "Стандарт" : "Про"
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

    @Override
    public String convertToDTO() {
        try {
            return mapper.writeValueAsString(KlingOptionsDTO.builder()
                    .aspectRatio(this.aspectRatio)
                    .prompt(this.prompt)
                    .duration(this.duration)
                    .mode(this.mode)
                    .withSound(this.withSound)
                    .imageUrls(this.imageUrls)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
