package com.example.tgbot.scheduler;

import com.example.tgbot.domain.enums.HistoryOperationType;
import com.example.tgbot.domain.model.ReferralLinks;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.repository.OperationsHistoryRepository;
import com.example.tgbot.repository.ReferralLinksRepository;
import com.example.tgbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Раз в смену периода (1-е и 15-е число) накапливает прибыль рефералов в ambassador_profit_total.
 * Прибыль периода * коэффициент амбассадора добавляется к общей сумме.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AmbassadorProfitScheduler {

    private final UserRepository userRepository;
    private final ReferralLinksRepository referralLinksRepository;
    private final OperationsHistoryRepository operationsHistoryRepository;

    @Scheduled(cron = "0 5 0 1,15 * *")
    @Transactional
    public void accumulateProfitOnPeriodChange() {
        var period = computePeriodToClose();
        if (period == null) {
            return;
        }
        LocalDate periodEnd = period[1];
        LocalDateTime from = period[0].atStartOfDay();
        LocalDateTime to = period[1].atTime(LocalTime.MAX);

        List<User> ambassadors = userRepository.findByAmbassadorTrue();
        for (User ambassador : ambassadors) {
            if (shouldSkipAccumulation(ambassador, periodEnd)) {
                continue;
            }
            BigDecimal profitRaw = calculateProfitForPeriod(ambassador, from, to);
            BigDecimal coefficient = ambassador.getAmbassadorProfitCoefficient() != null
                    ? ambassador.getAmbassadorProfitCoefficient()
                    : BigDecimal.ONE;
            BigDecimal toAdd = profitRaw.multiply(coefficient).setScale(2, RoundingMode.HALF_UP);

            BigDecimal currentTotal = ambassador.getAmbassadorProfitTotal() != null
                    ? ambassador.getAmbassadorProfitTotal()
                    : BigDecimal.ZERO;
            ambassador.setAmbassadorProfitTotal(currentTotal.add(toAdd));
            ambassador.setLastAccumulatedPeriodEnd(periodEnd);
            userRepository.save(ambassador);
            log.info("Ambassador {} period {}-{}: profit_raw={} coefficient={} added={} total={}",
                    ambassador.getId(), period[0], periodEnd, profitRaw, coefficient, toAdd,
                    ambassador.getAmbassadorProfitTotal());
        }
    }

    /**
     * @return [periodStart, periodEnd] или null если не день накопления (1-е или 15-е)
     */
    private LocalDate[] computePeriodToClose() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        if (day == 1) {
            LocalDate prevMonth = today.minusMonths(1);
            return new LocalDate[]{prevMonth.withDayOfMonth(15), prevMonth.withDayOfMonth(prevMonth.lengthOfMonth())};
        }
        if (day == 15) {
            return new LocalDate[]{today.withDayOfMonth(1), today.withDayOfMonth(14)};
        }
        return null;
    }

    private boolean shouldSkipAccumulation(User ambassador, LocalDate periodEnd) {
        LocalDate last = ambassador.getLastAccumulatedPeriodEnd();
        return last != null && !last.isBefore(periodEnd);
    }

    private BigDecimal calculateProfitForPeriod(User ambassador, LocalDateTime from, LocalDateTime to) {
        List<String> linkStrings = referralLinksRepository.findByCreator(ambassador).stream()
                .map(ReferralLinks::getLink)
                .collect(Collectors.toList());
        if (linkStrings.isEmpty()) {
            return BigDecimal.ZERO;
        }
        var referred = userRepository.findByLinkUsedIn(linkStrings);
        if (referred.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Timestamp fromTs = Timestamp.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp toTs = Timestamp.from(to.atZone(ZoneId.systemDefault()).toInstant());
        double revenue = operationsHistoryRepository.sumAbsBalanceChangeForGeneration(
                HistoryOperationType.GENERATION_REQUEST, referred, fromTs, toTs);
        BigDecimal cost = operationsHistoryRepository.sumCostRubForGeneration(
                HistoryOperationType.GENERATION_REQUEST, referred, fromTs, toTs);
        return BigDecimal.valueOf(revenue).subtract(cost != null ? cost : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
