package com.example.tgbot.models.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request wrapper for web interface generation.
 * Contains userId (User.telegramId) and model-specific options DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebGenerateRequest<T> {
    private Long userId;
    private T options;
}
