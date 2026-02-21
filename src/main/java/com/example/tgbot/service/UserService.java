package com.example.tgbot.service;

import com.example.tgbot.bot.UserSession;
import com.example.tgbot.data.HistoryOperationType;
import com.example.tgbot.model.OperationsHistory;
import com.example.tgbot.model.User;
import com.example.tgbot.repository.OperationsHistoryRepository;
import com.example.tgbot.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final OperationsHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;


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

        historyRepository.save(OperationsHistory.builder()
                .balanceChange((float) amount)
                .userId(user)
                .generationRequestInput(null)
                .operationType(HistoryOperationType.BALANCE_CHANGE)
                .build());
        return userRepository.save(user);
    }

    @Transactional
    public User consumeOneGeneration(User user, UserSession session) {
        int current = user.getBalance();
        int genPrice = session.getModel().getPrice();
        user.setBalance(current - genPrice);

        String strPayload = null;
        try {
            strPayload = objectMapper.writeValueAsString(session.getPayload());
        } catch (JsonProcessingException e) {
            log.error("Mapping exception e: {}", e.getMessage());
        }
        historyRepository.save(OperationsHistory.builder()
                .balanceChange((float) (genPrice * -1))
                .userId(user)
                .generationRequestInput(strPayload)
                .operationType(HistoryOperationType.GENERATION_REQUEST)
                .build());
        return userRepository.save(user);
    }

    @Transactional
    public User addGift(User user) {
        user.setBalance(user.getBalance() + 100);
        user.setBonusReceived(true);

        historyRepository.save(OperationsHistory.builder()
                .balanceChange(100F)
                .userId(user)
                .generationRequestInput(null)
                .operationType(HistoryOperationType.GIFT)
                .build());
        return userRepository.save(user);
    }

    public boolean checkBalanceBeforeGeneration(User user, UserSession session) {
        int current = user.getBalance();
        int genPrice = session.getModel().getPrice();
        return current >= genPrice;
    }
}