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
    /** ID записи в таблице operations_history (для удаления и т.д.). */
    private String id;
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
    /** Статус генерации: REQUESTED, PROCESSING, SUCCESS, FAILED. null для не-генераций или старых записей. */
    private String status;
    /** ID задачи от внешнего API. null, если task_id ещё не получен. */
    @JsonProperty("taskId")
    private String taskId;

    /** Конструктор без id (для обратной совместимости). */
    public HistoryItemDTO(Map<String, Object> options, Float balanceChange, String date,
                          List<?> resultItems, String model, String status, String taskId) {
        this(null, options, balanceChange, date, resultItems, model, status, taskId);
    }
}
