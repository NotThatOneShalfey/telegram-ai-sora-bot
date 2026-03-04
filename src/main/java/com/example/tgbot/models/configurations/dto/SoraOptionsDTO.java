package com.example.tgbot.models.configurations.dto;

import com.example.tgbot.models.enums.GenerationModel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Builder
@Data
public class SoraOptionsDTO {
    private String prompt;
    private String aspectRatio;
    private final String nFrames;
    private List<String> imageUrls;
}
