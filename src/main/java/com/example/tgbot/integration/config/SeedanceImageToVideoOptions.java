package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.SeedanceImageToVideoOptionsDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Опции для Seedance 2.0 (text-to-video и image-to-video).
 * Формат inputs соответствует Seedance API: prompt, duration, urls (опционально), resolution, aspectRatio.
 */
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeedanceImageToVideoOptions implements IModelRequestOptions {

    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    private final GenerationModel model = GenerationModel.SEEDANCE_2_0;
    @JsonInclude
    private String prompt;
    @Builder.Default
    @JsonInclude
    private Integer duration = 5;
    @JsonInclude
    private String resolution;
    @JsonInclude
    private String aspectRatio;
    @Builder.Default
    @JsonInclude
    private List<String> urls = new ArrayList<>();

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt != null ? prompt : "");
        input.put("duration", String.valueOf(duration != null ? duration : 5));
        if (urls != null && !urls.isEmpty()) {
            input.put("urls", urls);
        }
        if (resolution != null && !resolution.isBlank()) {
            input.put("resolution", resolution);
        }
        if (aspectRatio != null && !aspectRatio.isBlank()) {
            input.put("aspectRatio", aspectRatio);
        }
        return input;
    }

    @Override
    public String getOptionsText() {
        return String.format("""
                Seedance 2.0
                Длительность: %d сек | Изображений: %d
                """, duration != null ? duration : 5, urls != null ? urls.size() : 0);
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
            return mapper.writeValueAsString(SeedanceImageToVideoOptionsDTO.builder()
                    .prompt(this.prompt)
                    .duration(this.duration)
                    .resolution(this.resolution)
                    .aspectRatio(this.aspectRatio)
                    .urls(this.urls)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
