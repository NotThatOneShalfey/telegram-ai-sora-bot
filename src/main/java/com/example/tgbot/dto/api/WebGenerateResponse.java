package com.example.tgbot.dto.api;

import com.example.tgbot.domain.enums.GenerationModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for completed web generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebGenerateResponse<T> {
    /** URL/элементы результата: для Suno — [{audioUrl, imageUrl, title}], для других — ["url"] или [{url}]. В JSON как resultUrls. */
    @JsonProperty("resultUrls")
    private List<?> resultItems;
    private GenerationModel model;
    private int balanceChange;
    private T options;
}
