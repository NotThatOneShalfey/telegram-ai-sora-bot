package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.button.ButtonType;
import com.example.tgbot.telegram.button.IButton;
import com.example.tgbot.telegram.button.enums.AspectRatioEnum;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class KlingFormatSelectionPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    public KlingFormatSelectionPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot) {
        super(buttonRegistry, tgBot);
    }

    @Override
    public void execute(UserSession session) {
        super.executeSendMessage(session, getText(), getKeyboard(), false);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.KLING_FORMAT_SELECTION;
    }

    public String getText() {
        String text = """
                📽️Выберите удобный формат📽️
                """;
        return text;
    }

    public InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(ButtonType.ASPECT_RATIO_SELECTION).getKeyboardButton(GenerationModel.KLING_3_0, AspectRatioEnum.FORMAT_16_9),
                super.getButton(ButtonType.ASPECT_RATIO_SELECTION).getKeyboardButton(GenerationModel.KLING_3_0, AspectRatioEnum.FORMAT_9_16)));
        rows.add(List.of(super.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
