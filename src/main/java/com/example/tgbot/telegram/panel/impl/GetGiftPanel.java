package com.example.tgbot.telegram.panel.impl;

import com.example.tgbot.registry.ButtonRegistry;
import org.springframework.context.annotation.Lazy;
import com.example.tgbot.service.UserService;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panel.IChatPanel;
import com.example.tgbot.telegram.panel.PanelHelper;
import com.example.tgbot.telegram.panel.PanelType;
import com.example.tgbot.telegram.session.UserSession;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.tgbot.telegram.button.ButtonType.*;

@Component
public class GetGiftPanel extends AbstractSimpleMessagePanel implements IChatPanel {
    private final UserService userService;


    public GetGiftPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot, UserService userService) {
        super(buttonRegistry, tgBot);
        this.userService = userService;
    }

    @Override
    public void execute(UserSession session) {
        session.setUser(userService.addGift(session.getUser()));
        super.executeSendMessage(session, getText(), getKeyboard(), true);
    }

    @Override
    public PanelType getLabel() {
        return getStaticLabel();
    }

    public static PanelType getStaticLabel() {
        return PanelType.MAIN_GET_GIFT;
    }

    private String getText() {
        String text = """
                \uD83C\uDF81 Поздравляем!
                
                Ты получил 100 монет!✨
                1 Монета = 1 Рублю
                Теперь ты можешь творить!
                """;
        return text;
    }

    private InlineKeyboardMarkup getKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        rows.add(List.of(super.getButton(MAIN_CREATE_IMAGE_CALL).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_CREATE_VIDEO_CALL).getKeyboardButton()));
        rows.add(List.of(super.getButton(MAIN_CREATE_MUSIC_CALL).getKeyboardButton()));
        rows.add(List.of(PanelHelper.getSupportButton(), super.getButton(RECHARGE_BALANCE_CALL).getKeyboardButton()));
        markup.setKeyboard(rows);
        return markup;
    }
}
