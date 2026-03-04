package com.example.tgbot.registry;

import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;

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
}
