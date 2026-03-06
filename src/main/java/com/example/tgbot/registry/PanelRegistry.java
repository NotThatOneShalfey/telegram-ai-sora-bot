package com.example.tgbot.registry;

import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PanelRegistry {
    private final Map<PanelType, IChatPanel> chatPanels = new ConcurrentHashMap<>();

    public PanelRegistry(Collection<IChatPanel> panelCollection) {
        panelCollection.forEach(p -> chatPanels.put(p.getLabel(), p));
    }

    public IChatPanel getChatPanel(PanelType panel) {
        return chatPanels.get(panel);
    }
}
