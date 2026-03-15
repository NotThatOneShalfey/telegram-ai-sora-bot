package com.example.tgbot.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр задач Seedance для polling.
 * Хранит taskId и время создания (первый запрос — через 1 мин, далее каждые 30 сек).
 */
@Component
public class SeedancePollingRegistry {

    private final Map<String, PollEntry> entries = new ConcurrentHashMap<>();

    public void register(String taskId) {
        entries.put(taskId, new PollEntry(System.currentTimeMillis()));
    }

    /** Восстановление из БД: регистрирует задачу с сохранёнными временами. */
    public void registerWithTimes(String taskId, long createdAt, Long lastPolledAt) {
        PollEntry entry = new PollEntry(createdAt);
        entry.setLastPolledAt(lastPolledAt);
        entries.put(taskId, entry);
    }

    /** Снимок для сохранения в БД при shutdown. */
    public List<PollSnapshot> getSnapshotForPersistence() {
        List<PollSnapshot> list = new ArrayList<>();
        for (Map.Entry<String, PollEntry> e : entries.entrySet()) {
            PollEntry pe = e.getValue();
            list.add(new PollSnapshot(e.getKey(), pe.getCreatedAt(), pe.getLastPolledAt()));
        }
        return list;
    }

    public record PollSnapshot(String taskId, long createdAt, Long lastPolledAt) {}

    public void unregister(String taskId) {
        entries.remove(taskId);
    }

    /** Задачи, по которым нужно сделать запрос recordInfo в данный момент. */
    public java.util.Set<String> getTasksDueForPoll() {
        long now = System.currentTimeMillis();
        java.util.Set<String> due = new java.util.HashSet<>();
        for (Map.Entry<String, PollEntry> e : entries.entrySet()) {
            if (shouldPoll(e.getValue(), now)) {
                due.add(e.getKey());
            }
        }
        return due;
    }

    /** Обновляет время последнего опроса после успешного запроса. */
    public void markPolled(String taskId) {
        PollEntry entry = entries.get(taskId);
        if (entry != null) {
            entry.setLastPolledAt(System.currentTimeMillis());
        }
    }

    private boolean shouldPoll(PollEntry entry, long now) {
        long createdAt = entry.getCreatedAt();
        Long lastPolled = entry.getLastPolledAt();
        if (lastPolled == null) {
            return (now - createdAt) >= 60_000;
        }
        return (now - lastPolled) >= 30_000;
    }

    @lombok.Getter
    @lombok.Setter
    private static class PollEntry {
        private final long createdAt;
        private Long lastPolledAt;

        PollEntry(long createdAt) {
            this.createdAt = createdAt;
        }
    }
}
