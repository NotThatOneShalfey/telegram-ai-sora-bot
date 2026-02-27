package com.example.tgbot.models.configurations;

import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.buttons.enums.AspectRatioEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
@Slf4j
@ToString
public class SoraOptions implements ModelRequestOptions {

    private final ObjectMapper mapper = new JsonMapper();
    @Builder.Default
    private GenerationModel model = GenerationModel.SORA_2;
    private String prompt;
    @Builder.Default
    private String aspectRatio = "9:16";
    @Builder.Default
    private final String nFrames = "10";
    private String[] imageUrls;

    @Override
    public int getPrice() {
        return 0;
    }

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        if (imageUrls.length != 0) {
            input.put("image_urls", imageUrls);
        }
        input.put("aspect_ratio", convertAspectRatioForRequest());
        input.put("n_frames", nFrames);
        return input;
    }

    @Override
    public String getOptionsText() {
        String text = """
                
                ПАРАМЕТРЫ
                Модель: {0}
                Формат: {1}
                Длительность: {2}
                Режим: {3}
                
                """;

        return MessageFormat.format(text,
                model.getLocalizedModelName(),
                AspectRatioEnum.getButtonTextByValue(aspectRatio),
                nFrames + " секунд",
                "Стандарт"
        );
    }

    @Override
    public void setParametersFromJson(String json) {
        log.trace("Call setParametersFromJson. Json={}", json);
        try {
            SoraOptions opt = mapper.updateValue(this, mapper.readTree(json));
            log.trace("Current options Object -> {}", this);
            log.trace("Changed options Object -> {}", opt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertAspectRatioForRequest() {
        return aspectRatio.equals("16:9") ? "landscape" : "portrait";
    }
}
