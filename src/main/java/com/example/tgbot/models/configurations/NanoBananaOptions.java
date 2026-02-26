package com.example.tgbot.models.configurations;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
public class NanoBananaOptions implements ModelRequestOptions {
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
}
