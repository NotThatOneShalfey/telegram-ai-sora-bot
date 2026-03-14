package com.example.tgbot.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Элемент диалога для ElevenLabs Text-to-Dialogue.
 * text — текст реплики, voice — код голоса (с фронтенда).
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DialogueItemDTO {
    private String text;
    private String voice;
}
