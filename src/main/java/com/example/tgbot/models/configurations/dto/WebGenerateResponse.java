package com.example.tgbot.models.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for completed web generation.
 * Contains result links and model-specific options DTO (analog of WebGenerateRequest, but links instead of userName).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebGenerateResponse<T> {
    private List<String> links;
    private T options;
}
