package com.example.tgbot.models.configurations.dto;

import com.example.tgbot.telegram.buttons.enums.AspectRatioEnum;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class NanoBananaOptionsDTO {
    private String prompt;
    private List<String> imageInput;
    private String aspectRatio;
    private final String resolution;
    private final String outputFormat;
}
