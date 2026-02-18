package com.example.tgbot.service;

import com.example.tgbot.bot.UserSession;
import com.example.tgbot.model.User;
import com.example.tgbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public User findOrCreateUser(Long telegramId) {
        Optional<User> existing = userRepository.findByTelegramId(telegramId);
        if (existing.isPresent()) {
            return existing.get();
        }
        User newUser = User.builder()
                .telegramId(telegramId)
                .balance(0)
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
}