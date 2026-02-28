package com.example.tgbot;

import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RegistryService {
    private final Map<PanelType, IChatPanel> chatPanels = new ConcurrentHashMap<>();

    private final Map<ButtonType, IButton> buttons = new ConcurrentHashMap<>();
    private final Map<String, UserSession> waitingSessions = new ConcurrentHashMap<>();

    public RegistryService(Collection<IChatPanel> panelCollection,
                           Collection<IButton> buttonCollection) {

        panelCollection.forEach(p -> chatPanels.put(p.getLabel(), p));
        buttonCollection.forEach(b -> buttons.put(b.getLabel(), b));
    }

    public IChatPanel getChatPanel(PanelType panel) {
        return chatPanels.get(panel);
    }

    public IButton getButton(ButtonType button) {
        return buttons.get(button);
    }

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
