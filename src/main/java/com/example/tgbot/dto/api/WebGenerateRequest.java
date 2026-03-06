package com.example.tgbot.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request wrapper for web interface generation.
 * Contains userId (User.telegramId) as string; mapping to Long is done in the backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebGenerateRequest<T> {
    private String userId;
    private T options;
}
