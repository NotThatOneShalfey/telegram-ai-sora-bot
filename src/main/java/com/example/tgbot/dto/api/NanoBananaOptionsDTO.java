package com.example.tgbot.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class NanoBananaOptionsDTO {
    private String prompt;
    private List<String> imageInput;
    private String aspectRatio;
    private final String resolution;
    private final String outputFormat;
}
