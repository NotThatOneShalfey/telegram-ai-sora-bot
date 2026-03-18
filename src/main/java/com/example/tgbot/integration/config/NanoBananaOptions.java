package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.NanoBananaOptionsDTO;
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
public class NanoBananaOptions implements IModelRequestOptions {
    @JsonIgnore
    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    @Getter
    GenerationModel model = GenerationModel.NANO_BANANA_PRO;

    @Getter
    private String prompt;
    @Builder.Default
    private List<String> imageInput = new ArrayList<>();
    @Builder.Default
    private String aspectRatio = AspectRatioEnum.FORMAT_9_16.getValue();

    @Builder.Default
    private String resolution = "1K";

    @Builder.Default
    private String outputFormat = "png";


    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        input.put("image_input", imageInput);
        input.put("aspect_ratio", aspectRatio);
        input.put("resolution", resolution);
        input.put("output_format", outputFormat);
        return input;
    }

    @Override
    public String getOptionsText() {
        String text = """
                <pre>
                ПАРАМЕТРЫ
                Модель: {0}
                Формат: {1}
                </pre>
                """;

        return MessageFormat.format(text,
                model.getLocalizedModelName(),
                AspectRatioEnum.getButtonTextByValue(aspectRatio)
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

    public String convertToDTO() {
        try {
            return mapper.writeValueAsString(NanoBananaOptionsDTO.builder()
                    .aspectRatio(this.aspectRatio)
                    .prompt(this.prompt)
                    .imageInput(this.imageInput)
                    .outputFormat(this.outputFormat)
                    .resolution(this.resolution)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
