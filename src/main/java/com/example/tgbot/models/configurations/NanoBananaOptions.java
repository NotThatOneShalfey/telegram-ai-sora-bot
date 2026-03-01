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
public class NanoBananaOptions implements IModelRequestOptions {
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
    private final String resolution = "2K";

    @Builder.Default
    private final String outputFormat = "png";


    @Override
    public int getPrice() {
        return Math.round(6.95F*1.5F);
    }

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
                
                ПАРАМЕТРЫ
                Модель: {0}
                Формат: {1}
                
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

    public void setImageInput(List<String> imageInput) {
        if (this.imageInput.size() <= 8) {
            if (this.imageInput.size() + imageInput.size() <= 8) {
                this.imageInput.addAll(imageInput);
            } else {
                for (String image : imageInput) {
                    this.imageInput.add(image);
                    if (this.imageInput.size() == 8) {
                        break;
                    }
                }
            }
        }
    }
}
