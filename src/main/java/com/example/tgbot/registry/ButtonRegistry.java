package com.example.tgbot.registry;

import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ButtonRegistry {
    private final Map<ButtonType, IButton> buttons = new ConcurrentHashMap<>();

    public ButtonRegistry(Collection<IButton> buttonCollection) {
        buttonCollection.forEach(b -> buttons.put(b.getLabel(), b));
    }

    public IButton getButton(ButtonType button) {
        return buttons.get(button);
    }
}
