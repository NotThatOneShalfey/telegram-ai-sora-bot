package com.example.tgbot.models.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request wrapper for web interface generation.
 * Contains userName and model-specific options DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebGenerateRequest<T> {
    private String userName;
    private T options;
}
