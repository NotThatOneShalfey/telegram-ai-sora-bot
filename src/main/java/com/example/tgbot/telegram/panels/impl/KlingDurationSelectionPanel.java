package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.buttons.enums.PaidPackageEnum;
import com.example.tgbot.telegram.buttons.enums.VideoDurationEnum;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelHelper;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class KlingDurationSelectionPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public KlingDurationSelectionPanel(ObjectProvider<RegistryService> registryServiceProvider, TgBot tgBot) {
        super(registryServiceProvider, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.KLING_DURATION_SELECTION;
    }

    private String getText() {
        return """
                🎞 Длительность видео.
                Выберите продолжительность:
                """;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (VideoDurationEnum d : VideoDurationEnum.values()) {
            rows.add(List.of(super.getButton(ButtonType.KLING_DURATION_OPTION_SELECT).setParameters(d).getKeyboardButton()));
        }
        rows.add(List.of(super.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        rows.add(List.of(PanelHelper.getSupportButton()));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
