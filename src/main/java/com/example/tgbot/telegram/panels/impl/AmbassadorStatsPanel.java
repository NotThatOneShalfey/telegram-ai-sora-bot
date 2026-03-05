package com.example.tgbot.telegram.panels.impl;

import com.example.tgbot.service.AmbassadorStatsService;
import com.example.tgbot.telegram.panels.PanelType;
import com.example.tgbot.telegram.sessions.UserSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.example.tgbot.registry.ButtonRegistry;
import com.example.tgbot.telegram.TgBot;
import com.example.tgbot.telegram.panels.IChatPanel;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.example.tgbot.telegram.buttons.ButtonType.MAIN_MENU_CALL;

@Component
public class AmbassadorStatsPanel extends AbstractSimpleMessagePanel implements IChatPanel {

    private final AmbassadorStatsService ambassadorStatsService;

    public AmbassadorStatsPanel(@Lazy ButtonRegistry buttonRegistry, TgBot tgBot,
                               AmbassadorStatsService ambassadorStatsService) {
        super(buttonRegistry, tgBot);
        this.ambassadorStatsService = ambassadorStatsService;
    }

    @Override
    public void execute(UserSession session) {
        if (!session.getUser().isAmbassador()) {
            super.executeSendMessage(session, "У вас нет доступа к этой панели.", null, true);
            return;
        }
        int total = ambassadorStatsService.getReferralGenerationTotal(session.getUser());
        String period = ambassadorStatsService.getPeriodDescription();
        String text = """
                📊 Статистика амбассадора
                
                Период: %s
                
                💰 Общие затраты на генерацию абонентов по вашим реферальным ссылкам: %d монет
                """.formatted(period, total);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton())));
        super.executeSendMessage(session, text, keyboard, true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.AMBASSADOR_STATS;
    }
}
