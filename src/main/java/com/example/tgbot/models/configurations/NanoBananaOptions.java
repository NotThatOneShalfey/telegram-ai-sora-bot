package com.example.tgbot.models.configurations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
public class NanoBananaOptions implements ModelRequestOptions {
    private final ObjectMapper mapper = new JsonMapper();


    @Getter
    private String prompt;
    private String[] imageInput;
    private String aspectRatio;

    @Builder.Default
    private final String resolution = "2K";

    @Builder.Default
    private final String outputFormat = "png";


    @Override
    public int getPrice() {
        return 0;
    }

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        input.put("image_urls", imageInput);
        input.put("aspect_ratio", aspectRatio);
        input.put("resolution", resolution);
        input.put("output_format", outputFormat);
        return input;
    }

    @Override
    public String getOptionsText() {
        return null;
    }

    @Override
    public void setParametersFromJson(String json) {
        try {
            mapper.updateValue(this, mapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
