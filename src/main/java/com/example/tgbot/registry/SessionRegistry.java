package com.example.tgbot.registry;

import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {
    private final Map<String, UserSession> waitingSessions = new ConcurrentHashMap<>();

    public UserSession getWaitingSession(String taskId) {
        return waitingSessions.get(taskId);
    }

    public void putWaitingSession(String taskId, UserSession session) {
        waitingSessions.put(taskId, session);
    }

    public void removeWaitingSession(String taskId) {
        waitingSessions.remove(taskId);
    }

    /** Удаляет ожидающие сессии, не активные более заданного времени. */
    public int removeWaitingSessionsOlderThan(LocalDateTime cutoff) {
        int removed = 0;
        Iterator<Map.Entry<String, UserSession>> it = waitingSessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UserSession> e = it.next();
            LocalDateTime last = e.getValue().getLastActionDateTime();
            if (last != null && last.isBefore(cutoff)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }
}
