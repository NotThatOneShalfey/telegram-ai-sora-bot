package com.example.tgbot.dto.api;

import com.example.tgbot.domain.value.ErrorCode;
import lombok.Getter;

/**
 * Результат попытки постановки задачи на генерацию: успех или ошибка с кодом.
 */
@Getter
public class SubmitOutcome {
    private final WebSubmitResult success;
    private final ErrorCode error;

    private SubmitOutcome(WebSubmitResult success, ErrorCode error) {
        this.success = success;
        this.error = error;
    }

    public static SubmitOutcome ok(WebSubmitResult result) {
        return new SubmitOutcome(result, null);
    }

    public static SubmitOutcome fail(ErrorCode errorCode) {
        return new SubmitOutcome(null, errorCode);
    }

    public boolean isSuccess() {
        return success != null;
    }
}
