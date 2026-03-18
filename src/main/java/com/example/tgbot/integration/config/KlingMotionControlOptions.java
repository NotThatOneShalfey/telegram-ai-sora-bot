package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.KlingMotionControlOptionsDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * Опции для Kling 3.0 Motion Control (только web-интерфейс).
 * Формат input соответствует kie.ai API: input_urls, video_urls, prompt, mode, character_orientation.
 */
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlingMotionControlOptions implements IModelRequestOptions {

    @JsonIgnore
    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    private final GenerationModel model = GenerationModel.KLING_3_MOTION_CONTROL;
    @Builder.Default
    private List<String> inputUrls = new ArrayList<>();
    @Builder.Default
    private List<String> videoUrls = new ArrayList<>();
    private String prompt;
    @Builder.Default
    private String characterOrientation = "video";
    @Builder.Default
    private String mode = "720p";

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("input_urls", inputUrls != null ? inputUrls : List.of());
        input.put("video_urls", videoUrls != null ? videoUrls : List.of());
        input.put("prompt", prompt != null ? prompt : "");
        input.put("character_orientation", characterOrientation != null ? characterOrientation : "video");
        input.put("mode", mode != null ? mode : "720p");
        return input;
    }

    @Override
    public String getOptionsText() {
        return String.format("""
                Kling 3.0 Motion Control
                Режим: %s | Ориентация: %s
                Референсное изображение: %d | Референсное видео: %d
                """, mode, characterOrientation,
                inputUrls != null ? inputUrls.size() : 0,
                videoUrls != null ? videoUrls.size() : 0);
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
            return mapper.writeValueAsString(KlingMotionControlOptionsDTO.builder()
                    .inputUrls(this.inputUrls)
                    .videoUrls(this.videoUrls)
                    .prompt(this.prompt)
                    .characterOrientation(this.characterOrientation)
                    .mode(this.mode)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
