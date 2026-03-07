package com.example.tgbot.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryItemDTO {
    /** Опции запроса (параметры генерации). */
    private Map<String, Object> options;
    /** Изменение баланса (отрицательное при списании). */
    private Float balanceChange;
    /** Дата и время операции. */
    private String date;
    /** URL/элементы результата: для Suno — [{audioUrl, imageUrl, title}], для других — ["url"] или [{url}]. Пустой список, если не применимо. */
    @JsonProperty("resultUrls")
    private List<?> resultItems;
    /** Модель генерации (KLING_3_0, SORA_2, SUNO_V5, NANO_BANANA_PRO и т.д.), null для не-генераций. */
    private String model;
}
