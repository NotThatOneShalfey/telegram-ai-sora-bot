package com.example.tgbot.registry;

import com.example.tgbot.domain.enums.GenerationModel;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит результаты завершённых задач по taskId.
 * Заполняется при успешном callback из внешней системы.
 */
@Component
public class TaskResultRegistry {
    private final Map<String, TaskResultRecord> completedResults = new ConcurrentHashMap<>();

    public void put(String taskId, TaskResultRecord record) {
        completedResults.put(taskId, record);
    }

    public TaskResultRecord get(String taskId) {
        return completedResults.get(taskId);
    }

    public TaskResultRecord remove(String taskId) {
        return completedResults.remove(taskId);
    }

    @Data
    public static class TaskResultRecord {
        /** User.telegramId */
        private final Long userId;
        private final GenerationModel model;
        private final String optionsJson;
        private final List<String> links;
    }
}
