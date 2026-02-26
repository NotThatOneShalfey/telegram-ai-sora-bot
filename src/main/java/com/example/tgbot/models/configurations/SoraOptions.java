package com.example.tgbot.models.configurations;

import lombok.Builder;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
public class SoraOptions implements ModelRequestOptions {

    private String prompt;
    private String aspectRatio;
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
        input.put("aspect_ratio", getAspectRatio());
        input.put("n_frames", nFrames);
        return input;
    }

    @Override
    public String getOptionsText() {
        return null;
    }

    private String getAspectRatio() {
        return aspectRatio.equals("16:9") ? "portrait" : "landscape";
    }
}
