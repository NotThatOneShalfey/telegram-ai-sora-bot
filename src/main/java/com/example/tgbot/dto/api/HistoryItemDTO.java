package com.example.tgbot.dto.api;

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
    /** URL результатов после выполнения задания (пустой список, если не применимо). */
    private List<String> resultUrls;
    /** Модель генерации (KLING_3_0, SORA_2, SUNO_V5, NANO_BANANA_PRO и т.д.), null для не-генераций. */
    private String model;
}
