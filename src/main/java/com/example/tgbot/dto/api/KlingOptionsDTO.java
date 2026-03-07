package com.example.tgbot.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlingOptionsDTO {

    private String aspectRatio;
    private int duration;
    private boolean withSound;
    private String mode;
    private List<String> imageUrls;
    private String prompt;
}
