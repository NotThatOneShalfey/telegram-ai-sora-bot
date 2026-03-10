package com.example.tgbot.service;

import com.example.tgbot.domain.enums.HistoryOperationType;
import com.example.tgbot.domain.model.ReferralLinks;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.repository.OperationsHistoryRepository;
import com.example.tgbot.repository.ReferralLinksRepository;
import com.example.tgbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Статистика для амбассадоров: рефералы, прибыль (выручка − себестоимость), число генераций.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AmbassadorStatsService {

    private final ReferralLinksRepository referralLinksRepository;
    private final UserRepository userRepository;
    private final OperationsHistoryRepository operationsHistoryRepository;

    /** Новых рефералов за текущий период (по created_at) */
    public long getReferralCountNewInPeriod(User ambassador) {
        List<String> linkStrings = getLinkStrings(ambassador);
        if (linkStrings.isEmpty()) return 0;
        LocalDateTime[] range = getPeriodRange();
        return userRepository.countByLinkUsedInAndCreatedAtBetweenAndAmbassadorFalse(
                linkStrings,
                range[0].atZone(ZoneId.systemDefault()).toInstant(),
                range[1].atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Всего рефералов с реферальной ссылкой */
    public long getReferralCountTotal(User ambassador) {
        List<String> linkStrings = getLinkStrings(ambassador);
        if (linkStrings.isEmpty()) return 0;
        return userRepository.countByLinkUsedInAndAmbassadorFalse(linkStrings);
    }

    /** Прибыль за текущий период * коэффициент (для отображения) */
    public BigDecimal getReferralProfitForPeriod(User ambassador) {
        List<User> referred = getReferredUsers(ambassador);
        if (referred.isEmpty()) return BigDecimal.ZERO;
        LocalDateTime[] range = getPeriodRange();
        Timestamp from = Timestamp.from(range[0].atZone(ZoneId.systemDefault()).toInstant());
        Timestamp to = Timestamp.from(range[1].atZone(ZoneId.systemDefault()).toInstant());
        double revenue = operationsHistoryRepository.sumAbsBalanceChangeForGeneration(
                HistoryOperationType.GENERATION_REQUEST, referred, from, to);
        BigDecimal cost = operationsHistoryRepository.sumCostRubForGeneration(
                HistoryOperationType.GENERATION_REQUEST, referred, from, to);
        BigDecimal profitRaw = BigDecimal.valueOf(revenue).subtract(cost != null ? cost : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal coeff = ambassador.getAmbassadorProfitCoefficient() != null
                ? ambassador.getAmbassadorProfitCoefficient()
                : BigDecimal.ONE;
        return profitRaw.multiply(coeff).setScale(2, RoundingMode.HALF_UP);
    }

    /** Накопленная прибыль (из БД, закрытые периоды) */
    public BigDecimal getReferralProfitAllTime(User ambassador) {
        User fresh = userRepository.findById(ambassador.getId()).orElse(ambassador);
        BigDecimal total = fresh.getAmbassadorProfitTotal();
        return total != null ? total : BigDecimal.ZERO;
    }

    /** Число генераций по рефералам за текущий период */
    public long getReferralGenerationCountForPeriod(User ambassador) {
        List<User> referred = getReferredUsers(ambassador);
        if (referred.isEmpty()) return 0;
        LocalDateTime[] range = getPeriodRange();
        Timestamp from = Timestamp.from(range[0].atZone(ZoneId.systemDefault()).toInstant());
        Timestamp to = Timestamp.from(range[1].atZone(ZoneId.systemDefault()).toInstant());
        return operationsHistoryRepository.countGenerationsForUsers(
                HistoryOperationType.GENERATION_REQUEST, referred, from, to);
    }

    /** Число генераций по рефералам за всё время */
    public long getReferralGenerationCountAllTime(User ambassador) {
        List<User> referred = getReferredUsers(ambassador);
        if (referred.isEmpty()) return 0;
        return operationsHistoryRepository.countGenerationsForUsersAllTime(
                HistoryOperationType.GENERATION_REQUEST, referred);
    }

    /** Описание текущего периода: "01.mm - 14.mm" или "15.mm - dd.mm" (dd — последний день или сегодня) */
    public String getPeriodDescription() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        String mm = "%02d".formatted(today.getMonthValue());
        if (day < 15) {
            return "01.%s - 14.%s".formatted(mm, mm);
        }
        int lastDay = today.lengthOfMonth();
        return "15.%s - %02d.%s".formatted(mm, lastDay, mm);
    }

    private List<String> getLinkStrings(User ambassador) {
        List<ReferralLinks> links = referralLinksRepository.findByCreator(ambassador);
        return links.stream().map(ReferralLinks::getLink).collect(Collectors.toList());
    }

    private List<User> getReferredUsers(User ambassador) {
        List<String> linkStrings = getLinkStrings(ambassador);
        if (linkStrings.isEmpty()) return List.of();
        return userRepository.findByLinkUsedInAndAmbassadorFalse(linkStrings);
    }

    /** [from, to] — начало и конец текущего периода */
    private LocalDateTime[] getPeriodRange() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        LocalDateTime from = day < 15
                ? today.withDayOfMonth(1).atStartOfDay()
                : today.withDayOfMonth(15).atStartOfDay();
        LocalDateTime to = LocalDateTime.now();
        return new LocalDateTime[]{from, to};
    }
}
