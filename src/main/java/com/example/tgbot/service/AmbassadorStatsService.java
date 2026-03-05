package com.example.tgbot.service;

import com.example.tgbot.data.HistoryOperationType;
import com.example.tgbot.db.ReferralLinks;
import com.example.tgbot.db.User;
import com.example.tgbot.db.repositories.OperationsHistoryRepository;
import com.example.tgbot.db.repositories.ReferralLinksRepository;
import com.example.tgbot.db.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Статистика для амбассадоров: затраты на генерацию пользователей по их реферальным ссылкам.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AmbassadorStatsService {

    private final ReferralLinksRepository referralLinksRepository;
    private final UserRepository userRepository;
    private final OperationsHistoryRepository operationsHistoryRepository;

    /**
     * Сумма изменений баланса на генерацию (GENERATION_REQUEST) для пользователей,
     * пришедших по реферальным ссылкам данного амбассадора, за текущий период.
     * Период: 1–14 число — с 1-го по текущий день; 15+ — с 15-го по текущий день.
     */
    public int getReferralGenerationTotal(User ambassador) {
        List<ReferralLinks> links = referralLinksRepository.findByCreator(ambassador);
        if (links.isEmpty()) return 0;

        List<String> linkStrings = links.stream()
                .map(ReferralLinks::getLink)
                .collect(Collectors.toList());
        List<User> referredUsers = userRepository.findByLinkUsedIn(linkStrings);
        if (referredUsers.isEmpty()) return 0;

        LocalDateTime from = computePeriodStart();
        LocalDateTime to = LocalDateTime.now();
        Timestamp fromTs = Timestamp.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp toTs = Timestamp.from(to.atZone(ZoneId.systemDefault()).toInstant());

        double sum = operationsHistoryRepository.sumAbsBalanceChangeForGeneration(
                HistoryOperationType.GENERATION_REQUEST,
                referredUsers,
                fromTs,
                toTs);
        return (int) Math.round(sum);
    }

    /** Начало периода: 1-е число 00:00 или 15-е число 00:00 текущего месяца */
    public LocalDateTime computePeriodStart() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        if (day < 15) {
            return today.withDayOfMonth(1).atStartOfDay();
        }
        return today.withDayOfMonth(15).atStartOfDay();
    }

    /** Описание периода для отображения */
    public String getPeriodDescription() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        String month = today.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
        if (day < 15) {
            return "%s: с начала месяца по 15 число".formatted(month.toUpperCase());
        }
        return "%s: с 15го числа по конец месяца".formatted(month.toUpperCase());
    }
}
