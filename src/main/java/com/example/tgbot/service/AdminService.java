package com.example.tgbot.service;

import com.example.tgbot.domain.model.ReferralLinks;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.repository.ReferralLinksRepository;
import com.example.tgbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Сервис администрирования: установка статуса амбассадора и т.п.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private static final BigDecimal AMBASSADOR_PROFIT_COEFFICIENT = new BigDecimal("0.2");

    private final UserRepository userRepository;
    private final ReferralLinksRepository referralLinksRepository;

    /**
     * Устанавливает статус амбассадора пользователю по userName и добавляет реферальную ссылку.
     *
     * @param userName     userName пользователя в БД
     * @param referralLink реферальная ссылка для вставки в referral_links
     * @return true при успехе, false если пользователь не найден
     */
    @Transactional
    public boolean setAmbassadorStatus(String userName, String referralLink) {
        User user = userRepository.findByUserName(userName).orElse(null);
        if (user == null) {
            log.warn("AdminService: user not found by userName={}", userName);
            return false;
        }
        user.setAmbassador(true);
        user.setAmbassadorProfitCoefficient(AMBASSADOR_PROFIT_COEFFICIENT);
        userRepository.save(user);

        ReferralLinks rl = new ReferralLinks();
        rl.setLink(referralLink);
        rl.setCreated_by(user);
        referralLinksRepository.save(rl);

        log.info("AdminService: set ambassador status for userName={}, added referral link", userName);
        return true;
    }
}
