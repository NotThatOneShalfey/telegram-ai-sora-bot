package com.example.tgbot.models.configurations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Builder
@Setter
public class SoraOptions implements ModelRequestOptions {

    private final ObjectMapper mapper = new JsonMapper();

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

    @Override
    public void setParametersFromJson(String json) {
        try {
            mapper.updateValue(this, mapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAspectRatio() {
        return aspectRatio.equals("16:9") ? "portrait" : "landscape";
    }
}
