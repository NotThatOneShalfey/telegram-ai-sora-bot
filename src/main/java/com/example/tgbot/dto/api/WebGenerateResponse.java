package com.example.tgbot.dto.api;

import com.example.tgbot.domain.enums.GenerationModel;
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
    private List<String> resultUrls;
    private GenerationModel model;
    private int balanceChange;
    private T options;
}
