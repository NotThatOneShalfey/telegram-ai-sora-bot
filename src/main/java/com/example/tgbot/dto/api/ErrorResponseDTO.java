package com.example.tgbot.dto.api;

import com.example.tgbot.domain.value.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String code;
    private String description;

    public static ErrorResponseDTO from(ErrorCode errorCode) {
        return new ErrorResponseDTO(errorCode.getCode(), errorCode.getDescription());
    }
}
