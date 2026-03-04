package com.example.tgbot.models.configurations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class KlingOptionsDTO {

    private String aspectRatio;
    private int duration;
    private boolean withSound;
    private String mode;
    private boolean multiShots;
    private List<String> imageUrls;
    private String prompt;
    private List<MultiShotRequestDTO> multiShotRequestArray;

    @Builder
    public static class MultiShotRequestDTO {
        String prompt;
        int duration;
    }
}
