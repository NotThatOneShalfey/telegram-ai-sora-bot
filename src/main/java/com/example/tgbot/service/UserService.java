package com.example.tgbot.service;

import com.example.tgbot.data.HistoryOperationType;
import com.example.tgbot.db.OperationsHistory;
import com.example.tgbot.db.User;
import com.example.tgbot.db.repositories.OperationsHistoryRepository;
import com.example.tgbot.db.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public User findOrCreateUser(Long chatId) {
        Optional<User> existing = userRepository.findByTelegramId(chatId);
        if (existing.isPresent()) {
            return existing.get();
        }
        User newUser = User.builder()
                .telegramId(chatId)
                .balance(0)
                .build();
        return userRepository.save(newUser);
    }

    @Transactional
    public void updateUserCredentials(User user, String userName, String referralLink) {
        Optional<User> existing = userRepository.findById(user.getId());
        if (existing.isPresent()) {
            User userForUpdate = existing.get();
            boolean changed = false;
            if (!Objects.equals(userForUpdate.getUserName(), userName)) {
                userForUpdate.setUserName(userName);
                changed = true;
            }
            if (!Objects.equals(userForUpdate.getLinkUsed(), referralLink)) {
                userForUpdate.setLinkUsed(referralLink);
                changed = true;
            }
            // Если что нибудь поменялось, то сохраняем, если нет, то ничего не делаем
            if (changed) {
                userRepository.save(userForUpdate);
            }
        }
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

//    @Transactional
//    public User consumeOneGeneration(User user, UserSession session, int price) {
//        int current = user.getBalance();
//        user.setBalance(current - price);
//
//        String strPayload = null;
//        try {
//            strPayload = objectMapper.writeValueAsString(session.getPayload());
//        } catch (JsonProcessingException e) {
//            log.error("Mapping exception e: {}", e.getMessage());
//        }
//        historyRepository.save(OperationsHistory.builder()
//                .balanceChange((float) (price * -1))
//                .userId(user)
//                .generationRequestInput(strPayload)
//                .operationType(HistoryOperationType.GENERATION_REQUEST)
//                .build());
//        return userRepository.save(user);
//    }

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

//    public boolean checkBalanceBeforeGeneration(User user, UserSession session) {
//        int current = user.getBalance();
//        int genPrice = session.getModel().getPrice();
//        return current >= genPrice;
//    }
}