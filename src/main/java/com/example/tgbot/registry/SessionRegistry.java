package com.example.tgbot.registry;

import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {
    private final Map<String, WaitingTask> waitingTasks = new ConcurrentHashMap<>();

    public UserSession getWaitingSession(String taskId) {
        WaitingTask w = waitingTasks.get(taskId);
        return w != null ? w.session : null;
    }

    public TaskSource getTaskSource(String taskId) {
        WaitingTask w = waitingTasks.get(taskId);
        return w != null ? w.source : TaskSource.CHAT;
    }

    public void putWaitingSession(String taskId, UserSession session) {
        putWaitingSession(taskId, session, TaskSource.CHAT);
    }

    public void putWaitingSession(String taskId, UserSession session, TaskSource source) {
        waitingTasks.put(taskId, new WaitingTask(session, source != null ? source : TaskSource.CHAT));
    }

    public void removeWaitingSession(String taskId) {
        waitingTasks.remove(taskId);
    }

    private record WaitingTask(UserSession session, TaskSource source) {}

    /** Удаляет ожидающие сессии, не активные более заданного времени. */
    public int removeWaitingSessionsOlderThan(LocalDateTime cutoff) {
        int removed = 0;
        Iterator<Map.Entry<String, WaitingTask>> it = waitingTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, WaitingTask> e = it.next();
            LocalDateTime last = e.getValue().session().getLastActionDateTime();
            if (last != null && last.isBefore(cutoff)) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }
}
