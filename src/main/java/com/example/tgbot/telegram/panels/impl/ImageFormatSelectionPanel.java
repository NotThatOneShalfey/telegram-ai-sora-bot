package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.RegistryService;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.buttons.IButton;
import com.example.tgbot.telegram.buttons.enums.AspectRatioEnum;
import com.example.tgbot.telegram.buttons.ButtonType;
import com.example.tgbot.telegram.panels.IChatPanel;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImageFormatSelectionPanel extends AbstractSimpleMessagePanel implements IChatPanel {


    public ImageFormatSelectionPanel(RegistryService registryService, TgBot tgBot) {
        super(registryService, tgBot);
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
        return PanelType.GENERATE_IMAGE;
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
        IButton aspectRatioButton = super.registryService.getButton(ButtonType.ASPECT_RATIO_SELECTION).setParameters(GenerationModel.NANO_BANANA_PRO);
        rows.add(List.of(aspectRatioButton.setParameters(AspectRatioEnum.FORMAT_16_9).getKeyboardButton(),
                aspectRatioButton.setParameters(AspectRatioEnum.FORMAT_9_16).getKeyboardButton()));
        rows.add(List.of(super.registryService.getButton(ButtonType.MAIN_MENU_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
