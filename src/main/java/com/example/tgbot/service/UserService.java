package com.example.tgbot.service;

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
    public User consumeOneGeneration(User user) {
        int current = user.getBalance();
        if (current <= 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        user.setBalance(current - 1);
        return userRepository.save(user);
    }
}