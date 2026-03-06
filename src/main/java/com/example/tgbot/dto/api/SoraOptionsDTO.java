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
public class SoraOptionsDTO {
    private String prompt;
    private String aspectRatio;
    private final String nFrames;
    private List<String> imageUrls;
}
