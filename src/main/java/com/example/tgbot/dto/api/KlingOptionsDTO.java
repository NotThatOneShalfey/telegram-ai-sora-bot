package com.example.tgbot.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlingOptionsDTO {

    private String aspectRatio;
    private int duration;
    private boolean withSound;
    private String mode;
    private boolean multiShots;
    private List<String> imageUrls;
    private String prompt;
}
