package com.example.tgbot.service;

import com.example.tgbot.domain.model.User;
import com.example.tgbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Сервис запросов к данным пользователей.
 * Содержит только операции чтения и простой create при отсутствии.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findUser(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User findOrCreateUser(Long chatId) {
        return userRepository.findByTelegramId(chatId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .telegramId(chatId)
                            .balance(0)
                            .ambassador(false)
                            .ambassadorProfitCoefficient(BigDecimal.ZERO)
                            .ambassadorProfitTotal(BigDecimal.ZERO)
                            .balanceHold(0)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public User findUserByUserName(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }
}
