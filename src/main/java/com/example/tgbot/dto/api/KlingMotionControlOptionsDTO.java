package com.example.tgbot.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для Kling 3.0 Motion Control API (kie.ai).
 * Переносит движение из референсного видео на изображение персонажа.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KlingMotionControlOptionsDTO {

    /** Референсное изображение персонажа. JPEG, PNG, JPG. */
    private List<String> inputUrls;
    /** Референсное видео с движением. MP4, MOV. */
    private List<String> videoUrls;
    /** Описание желаемого результата (0–2500 символов). */
    private String prompt;
    /** Ориентация персонажа: "image" (макс 10 сек) или "video" (макс 30 сек). По умолчанию "video". */
    private String characterOrientation;
    /** Режим разрешения: "720p" (std) или "1080p" (pro). */
    private String mode;
}
