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
            super.executeSendMessage(session, "У вас нет доступа к этой панели.", null, false);
            return;
        }
        var user = session.getUser();
        long referralsNew = ambassadorStatsService.getReferralCountNewInPeriod(user);
        long referralsTotal = ambassadorStatsService.getReferralCountTotal(user);
        var profitPeriod = ambassadorStatsService.getReferralProfitForPeriod(user);
        var profitAllTime = ambassadorStatsService.getReferralProfitAllTime(user);
        long gensPeriod = ambassadorStatsService.getReferralGenerationCountForPeriod(user);
        long gensAllTime = ambassadorStatsService.getReferralGenerationCountAllTime(user);
        String period = ambassadorStatsService.getPeriodDescription();

        String text = """
                📊 Статистика амбассадора
                
                Период %s:
                👥 Приглашено: %d
                💰 Прибыль: %s ₽
                📈 Генераций: %d
                
                За всё время:
                👥 Приглашено: %d
                💰 Прибыль: %s ₽
                📈 Генераций: %d
                """.formatted(period, referralsNew, profitPeriod, gensPeriod, referralsTotal, profitAllTime, gensAllTime);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(List.of(super.getButton(MAIN_MENU_CALL).getKeyboardButton())));
        super.executeSendMessage(session, text, keyboard, true);
    }

    @Override
    public PanelType getLabel() {
        return PanelType.AMBASSADOR_STATS;
    }
}
