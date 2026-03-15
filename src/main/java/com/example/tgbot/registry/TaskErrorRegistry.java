package com.example.tgbot.registry;

import com.example.tgbot.domain.value.ErrorCode;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит ошибки задач для веб-интерфейса (polling).
 * Заполняется при callback "failed" из Kei AI API.
 */
@Component
public class TaskErrorRegistry {
    private final Map<String, TaskErrorRecord> errors = new ConcurrentHashMap<>();

    public void put(String taskId, Long userId, ErrorCode errorCode) {
        errors.put(taskId, new TaskErrorRecord(userId, errorCode));
    }

    public TaskErrorRecord get(String taskId) {
        return errors.get(taskId);
    }

    public TaskErrorRecord remove(String taskId) {
        return errors.remove(taskId);
    }

    /** Снимок для сохранения в БД при shutdown. */
    public Map<String, TaskErrorRecord> getAllSnapshot() {
        return new HashMap<>(errors);
    }

    @Data
    public static class TaskErrorRecord {
        private final Long userId;
        private final ErrorCode errorCode;
    }
}
