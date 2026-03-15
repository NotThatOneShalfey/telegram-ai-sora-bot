package com.example.tgbot.integration.config;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.dto.api.DialogueItemDTO;
import com.example.tgbot.dto.api.ElevenLabsOptionsDTO;
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
 * Опции для ElevenLabs Text-to-Dialogue V3 (только web-интерфейс).
 * Формат input соответствует kie.ai API: dialogue, stability, language_code.
 * stability=0.5, language_code="auto" по умолчанию.
 */
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElevenLabsOptions implements IModelRequestOptions {

    public static final int MAX_TOTAL_CHARS = 5000;
    private static final double DEFAULT_STABILITY = 0.5;
    private static final String DEFAULT_LANGUAGE_CODE = "auto";

    @JsonIgnore
    private final ObjectMapper mapper = new JsonMapper();

    @Builder.Default
    private final GenerationModel model = GenerationModel.ELEVENLABS_V3;
    @Builder.Default
    private List<DialogueItemDTO> dialogue = new ArrayList<>();
    @Builder.Default
    private Double stability = DEFAULT_STABILITY;
    @Builder.Default
    private String languageCode = DEFAULT_LANGUAGE_CODE;

    @Override
    public Map<String, Object> getRequestInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("dialogue", dialogue != null ? dialogue : List.of());
        input.put("stability", stability != null ? stability : DEFAULT_STABILITY);
        input.put("language_code", (languageCode != null && !languageCode.isBlank()) ? languageCode : DEFAULT_LANGUAGE_CODE);
        return input;
    }

    /** Сумма символов во всех text. */
    public int getTotalChars() {
        if (dialogue == null) return 0;
        return dialogue.stream()
                .mapToInt(d -> d != null && d.getText() != null ? d.getText().length() : 0)
                .sum();
    }

    @Override
    public String getOptionsText() {
        return String.format("""
                ElevenLabs V3
                Реплик: %d | Символов: %d
                """, dialogue != null ? dialogue.size() : 0, getTotalChars());
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
    public String getPrompt() {
        if (dialogue == null || dialogue.isEmpty()) return "";
        return dialogue.stream()
                .filter(d -> d != null && d.getText() != null)
                .map(DialogueItemDTO::getText)
                .reduce("", (a, b) -> a + b);
    }

    @Override
    public String convertToDTO() {
        try {
            return mapper.writeValueAsString(ElevenLabsOptionsDTO.builder()
                    .dialogue(this.dialogue)
                    .stability(this.stability)
                    .languageCode(this.languageCode)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
