package com.example.tgbot.service;

import com.example.tgbot.bot.UserSession;
import com.example.tgbot.model.User;
import com.example.tgbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public User findUser(Long telegramId) {
        Optional<User> existing = userRepository.findByTelegramId(telegramId);
        if (existing.isPresent()) {
            return existing.get();
        }
        log.error("User tgId = {} NOT FOUND!!!", telegramId);
        return null;
    }

    @Transactional
    public User createUser(Long telegramId, String userName, String referralLink) {
        Optional<User> existing = userRepository.findByTelegramId(telegramId);
        if (existing.isPresent()) {
            User user = existing.get();
            boolean changed = false;
            if (!Objects.equals(user.getUserName(), userName)) {
                user.setUserName(userName);
                changed = true;
            }
            if (!Objects.equals(user.getLinkUsed(), referralLink)) {
                user.setLinkUsed(referralLink);
                changed = true;
            }

            return changed ? userRepository.save(user) : user;
        }
        User newUser = User.builder()
                .telegramId(telegramId)
                .balance(0)
                .linkUsed(referralLink)
                .userName(userName)
                .build();
        return userRepository.save(newUser);
    }

    @Transactional
    public User addBalance(User user, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        user.setBalance(user.getBalance() + amount);
        return userRepository.save(user);
    }

    @Transactional
    public User consumeOneGeneration(User user, UserSession session) {
        int current = user.getBalance();
        int genPrice = session.getModel().getPrice();
        user.setBalance(current - genPrice);
        return userRepository.save(user);
    }

    @Transactional
    public User addGift(User user) {
        user.setBalance(user.getBalance() + 100);
        user.setBonusReceived(true);
        return userRepository.save(user);
    }

    public boolean checkBalanceBeforeGeneration(User user, UserSession session) {
        int current = user.getBalance();
        int genPrice = session.getModel().getPrice();
        return current >= genPrice;
    };

    @Transactional
    public User updateReferral(Long telegramId, String referral) {
        User user = userRepository.findByTelegramId(telegramId).orElse(null);
        try {
            if (user != null && user.getLinkUsed() == null) {
                user.setLinkUsed(referral);
                return userRepository.save(user);
            }
        } catch (DataIntegrityViolationException e) {
            log.error("Не существует реферальной ссылки {}, Ошибка: {}", referral, e.getMessage());
        }
        return user;
    }
}