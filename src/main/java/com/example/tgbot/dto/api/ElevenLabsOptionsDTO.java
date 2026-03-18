package com.example.tgbot.dto.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ElevenLabs Text-to-Dialogue V3 API (kie.ai).
 * Сумма символов во всех text не должна превышать 5000.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElevenLabsOptionsDTO {

    /** Массив реплик диалога. Каждый элемент: text, voice. */
    private List<DialogueItemDTO> dialogue;
    /** Стабильность голоса. По умолчанию 0.5. */
    @Builder.Default
    private Double stability = 0.5;
    /** Код языка. По умолчанию "auto". */
    @JsonProperty("languageCode")
    @JsonAlias("language_code")
    @Builder.Default
    private String languageCode = "auto";
}
