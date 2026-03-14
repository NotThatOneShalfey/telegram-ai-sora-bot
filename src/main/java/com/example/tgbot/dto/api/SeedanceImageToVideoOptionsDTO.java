package com.example.tgbot.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для Seedance 2.0 API (text-to-video и image-to-video).
 * urls — опционально; при наличии используется image-to-video.
 * duration — 4, 5, 8, 10 или 15 секунд.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeedanceImageToVideoOptionsDTO {

    /** Описание сцены (обязательно для text-to-video). */
    private String prompt;
    /** Длительность в секундах: 4, 5, 8, 10 или 15. */
    private Integer duration;
    /** Разрешение, например "720p". */
    private String resolution;
    /** Соотношение сторон, например "9:16". */
    private String aspectRatio;
    /** URL изображений для image-to-video (опционально). */
    private List<String> urls;
}
