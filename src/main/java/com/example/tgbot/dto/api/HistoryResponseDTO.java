package com.example.tgbot.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ответ истории операций: текущий баланс пользователя и список записей.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponseDTO {
    /** Текущий баланс пользователя. */
    private Integer balance;
    /** Список операций, отсортированный по дате по убыванию. */
    private List<HistoryItemDTO> items;
}
