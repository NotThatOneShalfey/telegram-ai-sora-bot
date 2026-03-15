package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.KlingOptionsDTO;
import com.example.tgbot.telegram.button.enums.AspectRatioEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlingOptions implements IModelRequestOptions {

    @JsonIgnore
    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    @Getter
    private final GenerationModel model = GenerationModel.KLING_3_0;
    @Builder.Default
    private String aspectRatio = "9:16";
    @Builder.Default
    @Getter
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
