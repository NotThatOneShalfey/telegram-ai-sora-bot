package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.SoraOptionsDTO;
import com.example.tgbot.telegram.button.enums.AspectRatioEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Setter
@Slf4j
@ToString
public class SoraOptions implements IModelRequestOptions {

    private final ObjectMapper mapper = new JsonMapper();
    @Builder.Default
    @Getter
    private GenerationModel model = GenerationModel.SORA_2;
    @Getter
    private String prompt;
    @Builder.Default
    private String aspectRatio = "9:16";
    @Builder.Default
    private String nFrames = "10";
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            input.put("image_urls", imageUrls);
        }
        input.put("aspect_ratio", convertAspectRatioForRequest());
        input.put("n_frames", nFrames);
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
                Режим: {3}
                </pre>
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
            mapper.updateValue(this, mapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertAspectRatioForRequest() {
        return aspectRatio.equals("16:9") ? "landscape" : "portrait";
    }

    public String convertToDTO() {
        try {
            return mapper.writeValueAsString(SoraOptionsDTO.builder()
                    .aspectRatio(this.aspectRatio)
                    .prompt(this.prompt)
                    .nFrames(this.nFrames)
                    .imageUrls(this.imageUrls)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
